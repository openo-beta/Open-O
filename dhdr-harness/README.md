# DHDR local conformance harness

A local stand-in for the OntarioMD DHDR endpoint, used to drive OpenO's DHDR viewer through
conformance scenarios that the live service cannot be made to produce on demand — empty results,
consent blocks, malformed entries, silent truncation, out-of-order dispense groups.

**This branch is a preservation copy, not a deliverable.** It is deliberately never merged. The DHDR
application code it exercises lives on `feature/dhdr-integration`; this branch is a sibling that
carries only the test harness.

## Why it exists

Several DHDR requirements can only be falsified with a response the real service will not return.
The clearest case is `crafted_group_order.json` and its generator: it is the only artifact that can
test DHDR04.02's between-group ordering and the "grouped format is NOT acceptable" clause in
DHDR05.01 / 08.01. Without it those clauses can be read but not checked.

## Contents

| Path | What it is |
|---|---|
| `harness/dhdr_stub.py` | the stub service — routes on patient HCN to a scenario |
| `harness/crafted_*.json` | 14 hand-built FHIR Bundles, one per structural case |
| `harness/make_*_fixture.py` | generators for the four fixtures that are derived rather than written |
| `harness/seed.sql` | seeds a `OneIdSession` row so the gateway will talk to the stub |
| `harness/restore_demo3.sql` | restores one test demographic to a known state |
| `SPEC_PROVENANCE.md` | which spec/IG package each conformance claim was checked against |

The stub routes by HCN: each seeded test demographic's health number maps to one scenario, so
selecting a patient in the EMR selects the response shape. The map is at the top of `dhdr_stub.py`.

## What is deliberately not here

- **OMD-supplied validation captures.** Real responses collected by OMD's Validation Team. Both the
  files and their filenames carry patient-style names, so neither is published. Four `omd_*` routes
  were removed from the stub's HCN table for the same reason. The crafted fixtures reconstruct every
  structural case those captures exercised.
- **Credentials** — `tokens.json` and the harness JKS. The JWTs that remain in `seed.sql` are
  stub-minted (`iss: http://localhost:8099`, `sub: harness-subject`), not real ONE ID tokens.
- **Request logs and captured traffic** — they contain health numbers from the drives.
- **Print baselines and screenshots** — rendered from capture data, so patient-shaped content.
- **Vendor IG packages** (~14 MB) — obtainable from OntarioMD, and not ours to republish.
  `SPEC_PROVENANCE.md` records which package version each claim was checked against.

## Running it

The harness also needed local patches to the connectivity layer that are **not** on this branch —
a ported `logInteraction`, a gateway-data attach in `OneIdFilter`, and an `OMDGatewayTransactionLog`
IDENTITY change. Those were local scaffolding against another team's module and were never proposed
as changes to it, so publishing them here would misrepresent them as such. Expect to re-do that
scaffolding to run the harness end to end.

The stub speaks plain HTTP. That is load-bearing rather than lazy: CXF applies `TLSClientParameters`
only to `https://` URLs, so an `http://` endpoint sidesteps mTLS entirely and no real keystore is
needed.
