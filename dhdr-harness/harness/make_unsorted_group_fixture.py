"""Build the DHDR04.02 / DHDR07.03 group-ordering fixture.

`crafted_grouping.json` deliberately makes 4 drugs that must NOT group, so it can prove the
3-point match. What it cannot show is what a *real* group does, because it never builds one:
head selection and within-group ordering are untested by it.

This fixture builds real groups and returns their members in ASCENDING date order -- the
order a FHIR server produces when it does not honour the `_sort=-whenprepared` hint the EMR
sends. `_sort` is a hint: a server MAY ignore it, so a requirement placed on the EMR Offering
cannot be discharged by assuming the response arrives pre-sorted.

Two DHDR04.02 clauses are decided by this fixture, and neither is decidable by any fixture
whose entries already descend:

  "A group is represented by displaying only the most recent event in that group according
   to the Dispensed Date."
  "Drug dispense events within an expanded group MUST be displayed in descending
   chronological order (with the most recent event first)."

and the DHDR07.03 equivalent for pharmacy services.

  drugs    3 dispenses identical on all three key parts (generic + strength + form), so
           they form exactly one group of 3. Emitted oldest first.
  services 3 events sharing one service type, so exactly one group of 3. Emitted oldest
           first.

A conformant viewer shows the 2016-05-20 drug and the 2016-09-03 service as the group heads
and lists each group's members newest-first, whatever order they arrived in.
"""
import copy
import json
import os

EXAMPLES = os.path.join(os.path.dirname(__file__), "..", "igcheck", "x-snapshot9",
                        "package", "examples")
OUT = os.path.join(os.path.dirname(__file__), "crafted_unsorted_group.json")

GEN = "http://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name"
DIN = "http://hl7.org/fhir/NamingSystem/ca-hc-din"
STRENGTH = "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-medication-strength"

# All three share generic + strength + form, so they are one group. Ascending on purpose.
DRUGS = [
    ("u1", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-01-05"),
    ("u2", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-03-14"),
    ("u3", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-05-20"),
]

# All three share a service type, so they are one group. Ascending on purpose.
SERVICES = [
    ("v1", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-07-11"),
    ("v2", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-08-02"),
    ("v3", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-09-03"),
]


def contained_medication(resource):
    for c in resource.get("contained", []):
        if c.get("resourceType") == "Medication":
            return c
    raise AssertionError("template entry has no contained Medication")


def set_coding(medication, system, display):
    for coding in medication.get("code", {}).get("coding", []):
        if coding.get("system") == system:
            coding["display"] = display
            return
    raise AssertionError("template Medication has no coding for " + system)


def build():
    sample = json.load(open(os.path.join(EXAMPLES, "BundleResponseSample.json")))
    by_category = {}
    for entry in sample["entry"]:
        code = entry["resource"]["category"]["coding"][0]["code"]
        by_category[code] = entry
    assert "product" in by_category and "service" in by_category, sorted(by_category)

    entries = []

    for suffix, generic, strength, form, brand, when in DRUGS:
        entry = copy.deepcopy(by_category["product"])
        res = entry["resource"]
        res["id"] = "CRAFTED-unsorted-" + suffix
        res["whenPrepared"] = when
        med = contained_medication(res)
        set_coding(med, GEN, generic)
        set_coding(med, DIN, brand)
        # Keyed off form.text by the viewer; set both so the fixture reads the same either way.
        med.setdefault("form", {})["text"] = form
        med["form"].setdefault("coding", [{}])[0]["display"] = form
        for ext in med.get("extension", []):
            if ext.get("url") == STRENGTH:
                ext["valueString"] = strength
                break
        else:
            raise AssertionError("template Medication has no strength extension")
        entries.append(entry)

    for suffix, svc_type, svc_desc, when in SERVICES:
        entry = copy.deepcopy(by_category["service"])
        res = entry["resource"]
        res["id"] = "CRAFTED-unsorted-" + suffix
        res["whenPrepared"] = when
        med = contained_medication(res)
        set_coding(med, DIN, svc_type)
        set_coding(med, GEN, svc_desc)
        entries.append(entry)

    bundle = {
        "resourceType": "Bundle",
        "id": "CRAFTED-unsorted-group",
        "type": "searchset",
        "total": len(entries),
        "link": [],
        "entry": entries,
    }
    json.dump(bundle, open(OUT, "w"), indent=1)
    return bundle


if __name__ == "__main__":
    b = build()
    print("wrote", OUT, "with", len(b["entry"]), "entries (emitted oldest-first)")
    for e in b["entry"]:
        r = e["resource"]
        print("  ", r["category"]["coding"][0]["code"], r["whenPrepared"], r["id"])
