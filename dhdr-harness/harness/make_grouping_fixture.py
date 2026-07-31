"""Build the UC06 grouping fixture (steps 29 and 30).

No shipped IG example exercises grouping: BundleResponseSample carries one product and one
service, so grouped and ungrouped render identically and neither step is decidable against it.

This derives from that sample's fully-contained entries -- so every field resolves and the
grouping key is built from real values, not from the undefined-everything case that the
external-reference entries in BundleSearch produce -- and varies only what the grouping keys
are built from:

  drugs    (DHDR04.02, 3-point match: genericName + strength + dosage form)
           4 dispenses, pairwise distinct on at least one of the three parts, so a
           conformant viewer groups NONE of them. Step 29 asserts exactly this.
           Two of them differ ONLY by strength and two ONLY by form, so a viewer that
           keyed on generic name alone would wrongly merge them -- the failure this
           fixture is built to catch.

  services (DHDR07.02, grouped by pharmacy service type alone)
           3 events: two sharing a service type (a group of 2) and one distinct
           (ungrouped), which is what step 30 steps 2 and 5 require together.

Dispense dates descend in a deliberate interleave so "most recent per group" and
"descending chronological" cannot both pass by accident of insertion order.
"""
import copy
import json
import os

EXAMPLES = os.path.join(os.path.dirname(__file__), "..", "igcheck", "x-snapshot9",
                        "package", "examples")
OUT = os.path.join(os.path.dirname(__file__), "crafted_grouping.json")

GEN = "http://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name"
DIN = "http://hl7.org/fhir/NamingSystem/ca-hc-din"
STRENGTH = "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-medication-strength"

# (id-suffix, generic, strength, form, brand, whenPrepared)
# Rows 0/1 share a generic and differ only by strength; rows 2/3 share a generic and differ
# only by form. Nothing shares all three, so nothing may group.
DRUGS = [
    ("d1", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen",   "2016-03-27"),
    ("d2", "ACETYLSALICYLIC ACID", "81mg",  "Enteric coated tablet", "Novasen",   "2016-02-11"),
    ("d3", "METFORMIN HYDROCHLORIDE", "500mg", "Tablet",             "Glucophage", "2016-03-02"),
    ("d4", "METFORMIN HYDROCHLORIDE", "500mg", "Extended release tablet", "Glumetza", "2016-01-08"),
]

# (id-suffix, service type [DIN display], description [generic display], whenPrepared)
# The first two share a service type and must form one group of 2; the third stands alone.
SERVICES = [
    ("s1", "Fluviral -15mcg/0.5mL;5mL Multidose Vial", "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-09-03"),
    ("s2", "Fluviral -15mcg/0.5mL;5mL Multidose Vial", "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-09-01"),
    ("s3", "MedsCheck Annual Medication Review",       "MEDSCHECK ANNUAL PROFESSIONAL SERVICE FEE",         "2016-08-15"),
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
        res["id"] = "CRAFTED-group-" + suffix
        res["whenPrepared"] = when
        med = contained_medication(res)
        set_coding(med, GEN, generic)
        set_coding(med, DIN, brand)
        # The viewer keys dosage form off form.text (index.jsp:2202), not form.coding.display --
        # the in-code comment above getUniqVal claims coding.display, which is stale. Set both so
        # the fixture varies whatever a reader looks at.
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
        res["id"] = "CRAFTED-group-" + suffix
        res["whenPrepared"] = when
        med = contained_medication(res)
        set_coding(med, DIN, svc_type)
        set_coding(med, GEN, svc_desc)
        entries.append(entry)

    bundle = {
        "resourceType": "Bundle",
        "id": "CRAFTED-grouping",
        "type": "searchset",
        "total": len(entries),
        "link": [],
        "entry": entries,
    }
    json.dump(bundle, open(OUT, "w"), indent=1)
    return bundle


if __name__ == "__main__":
    b = build()
    print("wrote", OUT, "with", len(b["entry"]), "entries")
    for e in b["entry"]:
        r = e["resource"]
        med = contained_medication(r)
        codings = {c["system"].split("/")[-1]: c.get("display") for c in med["code"]["coding"]}
        form = (med.get("form") or {}).get("coding", [{}])[0].get("display")
        strength = next((x.get("valueString") for x in med.get("extension", [])
                         if x.get("url") == STRENGTH), None)
        print(" ", r["category"]["coding"][0]["code"], r["whenPrepared"],
              "|", codings.get("ca-drug-gen-name"), "|", strength, "|", form,
              "|", codings.get("ca-hc-din"))
