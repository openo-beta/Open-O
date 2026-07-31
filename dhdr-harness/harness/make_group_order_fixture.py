"""Build the DHDR04.02 / DHDR07.02 *between-group* ordering fixture.

`crafted_unsorted_group.json` proves the two clauses about a group's *internal* state -- which
member heads the group, and what order its members list in when expanded. It cannot say anything
about the clause that orders the groups against each other:

  "Groups of drug dispense events MUST be ordered by Dispensed Date in descending chronological
   order (with the most recent event first)."

because it builds exactly one drug group and one service group. With a single group there is no
relative order to get wrong.

This fixture builds TWO drug groups and TWO service groups, and emits every event oldest-first
across both -- the order a FHIR server produces when it ignores the `_sort=-whenprepared` hint.
The dates are chosen so that the order the groups are first *encountered* is the reverse of the
order they must be *displayed* in:

  drugs     A = ASA 325mg Enteric coated tablet   Jan 5, Mar 14, May 20   -> head May 20
            B = METFORMIN 500mg Tablet            Feb 1, Apr 1,  Jun 1    -> head Jun 1
            emitted: Jan5(A) Feb1(B) Mar14(A) Apr1(B) May20(A) Jun1(B)
            first encountered: A then B   |   MUST display: B (Jun 1) then A (May 20)

  services  C = INFLUENZA ...  Jul 11, Aug 2,  Sep 3   -> head Sep 3
            D = MEDSCHECK ...  Jul 20, Aug 15, Oct 5   -> head Oct 5
            first encountered: C then D   |   MUST display: D (Oct 5) then C (Sep 3)

So a viewer that builds its group list in encounter order shows A before B and C before D, which
is ascending by group head -- exactly backwards. A viewer that sorts the heads shows B then A and
D then C. The two outcomes are unambiguous and cannot both be produced by a correct reading.

Note the *within*-group clauses stay satisfiable here too (each group is emitted ascending), so a
failure of this fixture isolates the between-group clause rather than re-testing #62.
"""
import copy
import json
import os

EXAMPLES = os.path.join(os.path.dirname(__file__), "..", "igcheck", "x-snapshot9",
                        "package", "examples")
OUT = os.path.join(os.path.dirname(__file__), "crafted_group_order.json")

GEN = "http://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name"
DIN = "http://hl7.org/fhir/NamingSystem/ca-hc-din"
STRENGTH = "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-medication-strength"

# (suffix, generic, strength, form, brand, whenPrepared) -- listed globally oldest-first, so the
# two groups interleave and group A is encountered before group B despite B being the newer group.
DRUGS = [
    ("d1", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-01-05"),
    ("d2", "METFORMIN HYDROCHLORIDE", "500mg", "Tablet", "Glucophage", "2016-02-01"),
    ("d3", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-03-14"),
    ("d4", "METFORMIN HYDROCHLORIDE", "500mg", "Tablet", "Glucophage", "2016-04-01"),
    ("d5", "ACETYLSALICYLIC ACID", "325mg", "Enteric coated tablet", "Novasen", "2016-05-20"),
    ("d6", "METFORMIN HYDROCHLORIDE", "500mg", "Tablet", "Glucophage", "2016-06-01"),
]

# (suffix, svc_type, svc_desc, whenPrepared) -- same construction on the service side.
SERVICES = [
    ("s1", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-07-11"),
    ("s2", "MedsCheck Annual",
     "MEDSCHECK ANNUAL PROFESSIONAL SERVICE FEE", "2016-07-20"),
    ("s3", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-08-02"),
    ("s4", "MedsCheck Annual",
     "MEDSCHECK ANNUAL PROFESSIONAL SERVICE FEE", "2016-08-15"),
    ("s5", "Fluviral -15mcg/0.5mL;5mL Multidose Vial",
     "INFLUENZA VACCINE PROGRAM PROFESSIONAL SERVICE FEE", "2016-09-03"),
    ("s6", "MedsCheck Annual",
     "MEDSCHECK ANNUAL PROFESSIONAL SERVICE FEE", "2016-10-05"),
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
        res["id"] = "CRAFTED-grouporder-" + suffix
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
        res["id"] = "CRAFTED-grouporder-" + suffix
        res["whenPrepared"] = when
        med = contained_medication(res)
        set_coding(med, DIN, svc_type)
        set_coding(med, GEN, svc_desc)
        entries.append(entry)

    bundle = {
        "resourceType": "Bundle",
        "id": "CRAFTED-group-order",
        "type": "searchset",
        "total": len(entries),
        "link": [],
        "entry": entries,
    }
    json.dump(bundle, open(OUT, "w"), indent=1)
    return bundle


if __name__ == "__main__":
    b = build()
    print("wrote", OUT, "with", len(b["entry"]), "entries (emitted oldest-first, groups interleaved)")
    for e in b["entry"]:
        r = e["resource"]
        print("  ", r["category"]["coding"][0]["code"], r["whenPrepared"], r["id"])
