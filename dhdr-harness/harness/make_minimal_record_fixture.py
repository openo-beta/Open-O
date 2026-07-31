"""Build the minimal-legal-record fixture.

Every other fixture in this harness descends from `BundleResponseSample`, which is fully
populated. So a large share of the "element X is displayed" passes recorded against steps 22,
23, 25, 26 and 36 are contingent on that one file happening to carry element X. A production
record may legally omit every optional element, and nothing here has ever asked what the
viewer does then.

This strips each entry down to what `MedicationDispense-consumer.json` and
`Medication-consumer.json` actually require, and drops the rest. Checked against those
profiles rather than guessed: `whenHandedOver` (pickup date), `type` (pharmacy service type),
the medication strength extension, `dosageInstruction` (dose + frequency) and `daysSupply` are
all optional, while `quantity`, `whenPrepared`, `category.coding`, `identifier.value` and the
four `Medication.code.coding` slices are required. Constraints such as
`dosageInstruction.timing.repeat.frequency` are min=1 only *within* an element that is itself
optional, so omitting the parent stays conformant -- which is why the shipped product example
can carry no `dosageInstruction` at all and still validate.

What this is looking for is NOT blank columns. A blank is correct: the requirements ask the
EMR to display these elements when the service supplies them. It is looking for

  * a blank that reads as a value -- literal "undefined", "null", "NaN", "[object Object]",
    or a units-only cell like "-- mg" that implies a quantity nobody sent,
  * a list that stops early or a view that throws, the way an unguarded deref did in #37,
  * a grouping key silently built from absent parts.

The two product entries are deliberately identical apart from their dispense date and id, so
with no strength extension their DHDR04.02 key agrees on all three parts and they must form
one group of two -- re-testing #62's head selection on sparse data.
"""
import copy
import json
import os

EXAMPLES = os.path.join(os.path.dirname(__file__), "..", "igcheck", "x-snapshot9",
                        "package", "examples")
OUT = os.path.join(os.path.dirname(__file__), "crafted_minimal_record.json")

# Kept on the dispense. Everything absent from this list is optional under the consumer
# profile and is dropped: daysSupply, dosageInstruction, whenHandedOver, type, text, meta.
DISPENSE_KEEP = {
    "resourceType", "id", "contained", "status", "identifier", "category",
    "medicationReference", "subject", "performer", "authorizingPrescription",
    "quantity", "whenPrepared",
}

# Kept per contained resource type. Dropping Practitioner/Organization `telecom` removes the
# prescriber phone and the pharmacy fax -- both are elements the summary and detail views bind,
# and neither is required by the profile.
CONTAINED_KEEP = {
    "Medication": {"resourceType", "id", "code", "form"},          # drops the strength extension
    "Patient": {"resourceType", "id", "identifier", "name", "birthDate", "gender"},
    "Practitioner": {"resourceType", "id", "identifier", "name"},  # drops telecom
    "Organization": {"resourceType", "id", "identifier", "name"},  # drops telecom
    "MedicationRequest": {"resourceType", "id", "status", "intent", "medicationReference",
                          "subject", "requester"},
}

# (id-suffix, whenPrepared). Same drug, so they must group.
PRODUCTS = [("m1", "2016-04-10"), ("m2", "2016-06-22")]
SERVICES = [("n1", "2016-05-05")]


def strip(resource):
    out = {k: v for k, v in resource.items() if k in DISPENSE_KEEP}
    contained = []
    for c in resource.get("contained", []):
        keep = CONTAINED_KEEP.get(c["resourceType"])
        if keep is None:
            raise AssertionError("unhandled contained type " + c["resourceType"])
        contained.append({k: v for k, v in c.items() if k in keep})
    out["contained"] = contained
    return out


def build():
    sample = json.load(open(os.path.join(EXAMPLES, "BundleResponseSample.json")))
    by_category = {}
    for entry in sample["entry"]:
        by_category[entry["resource"]["category"]["coding"][0]["code"]] = entry
    assert "product" in by_category and "service" in by_category, sorted(by_category)

    entries = []
    for suffix, when in PRODUCTS:
        entry = copy.deepcopy(by_category["product"])
        entry["resource"] = strip(entry["resource"])
        entry["resource"]["id"] = "CRAFTED-min-" + suffix
        entry["resource"]["whenPrepared"] = when
        entries.append(entry)

    for suffix, when in SERVICES:
        entry = copy.deepcopy(by_category["service"])
        entry["resource"] = strip(entry["resource"])
        entry["resource"]["id"] = "CRAFTED-min-" + suffix
        entry["resource"]["whenPrepared"] = when
        entries.append(entry)

    bundle = {
        "resourceType": "Bundle",
        "id": "CRAFTED-minimal-record",
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
        print("  ", r["category"]["coding"][0]["code"], r["whenPrepared"], r["id"],
              "| dispense keys:", sorted(k for k in r if k != "contained"))
        for c in r["contained"]:
            print("        ", c["resourceType"],
                  sorted(k for k in c if k not in ("resourceType", "id")))
