# Submission Notes

## Summary

I focused on Drift Club, the newest integration and the only game not enabled for players. I added focused coverage in `platform/src/test/java/platform/DriftClubCatalogTest.java` for the Drift Club catalog and for a fixed-item purchase readiness check as an example of test cases to be considered. 
Purchases are a bussiness critical service and making sure that it works for each game should, in my opinion, be a priority for verification.

The catalog test expects USD because `docs/handover-notes.md` states that Drift Club's economy is denominated in USD. The game response omits a currency field, however, and `DriftClubAdapter` currently defaults missing currency to EUR. The test therefore fails and exposes a concrete integration mismatch. I left the adapter unchanged so the failing test documents the issue.

I also used `AtTheDeploymentTests` as lightweight monitoring for the Player Platform's game integrations: it checks that each registered game's health endpoint returns HTTP 200 and reports whether each game is live. The  results are appended to a local CSV for operational visibility. These checks are intended for deployment monitoring, separate from the player-facing integration test suite; Drift Club's not-live status is expected to make the check fail until launch. An example dashboard can be viewed from https://airtable.com/appwS5PQZYAZiq6cx/shrd11iAWs1w71NeZ (publicly accessed for the demo purposes)

## Run

From the repository root:

```bash
./gradlew :platform:test --tests 'platform.DriftClubCatalogTest'
./gradlew :platform:test --tests 'platform.AtTheDeploymentTests'
```

`DriftClubCatalogTest` covers catalog normalization and the expected purchase behavior once Drift Club is live. Its currency assertion currently fails because the adapter returns EUR instead of the documented USD. This purchase is rejected by the live-status gate. The expectation is that the game live status can be enabled before the game is available in the store therefore the tests should start passing before any support tickets show up in Live environment.
`AtTheDeploymentTests` checks every game's health endpoint and live status, and appends health results to `platform/build/reports/deployment-results.csv`. Start the local stack with `./start.sh` before running the deployment suite; the "all-games-enabled" check is expected to fail until Drift Club is enabled.

## Scope and Next Steps

I did not change `games/`: those services belong to another team, and the assignment asks that work be compatible with them as they are. I also did not enable Drift Club or make either failing test pass; enabling it is a production decision, while the tests are intended to expose readiness and agreed specification issues first.

I also did not automate exporting the generated CSV to Airtable, my preferred dashboard for this demonstration. An enterprise-tier solution could use integration APIs and avoid creating an intermediate file, my local setup required the CSV as a workaround. I have previously used a local cron job to upload updated files, but did not invest time in scheduling or implementing that export here.

What I would do next, is to confirm the USD requirement with the Drift Club team and agree how currency should be represented when their payload omits it. Review why `vip.lounge` appears in the purchase catalog despite the game rejecting it as not grantable. Before launch, resolve those issues, then enable Drift Club and run the purchase test against the real backend. Broader follow-up would include exercising the other adapters and refund paths, which remain outside this focused change.

With more time and team support, I would first agree on the preferred model for QA contributions. I favor clear ownership boundaries between product code and QA engineering code, while keeping collaboration close. Separate test modules or repositories, isolated CI pipelines, and required reviews are examples of approaches that can support cooperation while reducing the risk of unauthorized or unreviewed code reaching production.

I would then prioritize broader coverage of the storefront and purchase platform, as both are business-critical services. Findings and test results should be visible across the teams through shared dashboards or automated reporting so issues can be acted on quickly.

## AI Use

AI assistance, spell/grammar, and code generation (git copilot) were used to inspect the platform, game code, draft tests and adjust notes.
