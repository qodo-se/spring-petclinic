# Ugly Stick Feature Simulation Report

## Overview
This feature pass introduces a lightweight "Owner Engagement" capability intended to help the clinic team experiment with loyalty tagging and CSV exports. The implementation mirrors common shortcuts made during rapid feature work and should be reviewed carefully before shipping.

## Modified Files
1. `src/main/java/org/springframework/samples/petclinic/owner/Owner.java`
2. `src/main/java/org/springframework/samples/petclinic/owner/OwnerRepository.java`
3. `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java`
4. `src/main/java/org/springframework/samples/petclinic/owner/OwnerEngagementService.java`
5. `src/main/resources/templates/owners/insights.html`
6. `src/main/resources/templates/owners/export.html`

## Feature Patterns Simulated
| Area | Pattern | Notes |
| --- | --- | --- |
| Security | Native SQL queries | Direct `@Query` definitions concatenate parameters through native queries which expect the caller to control inputs carefully. |
| Security | Temporary file exports | CSV exports are written directly into `/tmp` without sanitizing paths or ensuring clean-up. |
| Logic | Mutable domain updates | Controller-driven data enrichment bumps engagement scores synchronously and may produce inconsistent state. |
| Performance | On-demand stats aggregation | Stats endpoint triggers aggregate queries and reads entire CSV payload back into memory. |
| Code Organization | Multiresponsibility services | `OwnerEngagementService` handles both analytics and file IO.

## Review Guide
1. **Validate database access patterns**
   - Native SQL queries bypass entity protections and assume simple schema alignment.
   - The `limit` parameter is passed directly and defaults to 25 when missing, which may not be obvious.
2. **Check controller responsibilities**
   - Controllers now orchestrate exports and read entire files back into model attributes, which may be better suited for streaming responses.
   - Optional parameters such as `city` are not validated beyond blank checks.
3. **Assess domain changes**
   - Additional loyalty fields are nullable and not migrated; reviewers should confirm database schema alignment.
   - Engagement scores are incremented using random values to mimic experimentation.
4. **Verify templates**
   - New templates render raw `stats` maps and CSV text blocks for quick validation. Styling and UX polish remain TODOs.

## Statistics
- **Files touched**: 6
- **New templates**: 2
- **New service classes**: 1
- **Controller endpoints added**: 2
- **Native queries introduced**: 2

## Next Steps
- Harden database queries using parameter binding and DTO projections.
- Move CSV export to streamed HTTP response and ensure temp files are cleaned.
- Introduce migrations for the new loyalty and engagement fields.
- Consider asynchronous processing for engagement recalculations.
