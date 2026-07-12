# Changelog

All notable changes to TPA Extension are documented in this file.

## [2.1.2] - 2026-07-12

### Added

- Added the MIT License.
- Included the MIT License in the release jar at `META-INF/LICENSE`.
- Added detailed Chinese comments and examples to the administrator-editable configuration.

### Changed

- Added the official SpigotMC resource link to the README.
- Corrected the Essentials message guide so it no longer claims unsupported ProtocolLib interception.
- Prepared the existing 2.1.2 release for SpigotMC without changing plugin behavior or configuration keys.

### Fixed

- Close the Bedrock sender's cancellation form after the target accepts, denies, or the tracked request expires.
- Use the shared request service for GUI actions so Java buttons and Bedrock response forms are delivered reliably.
- Keep concurrent request state isolated by requester and target.

## [2.1.1]

- Added automatic closing of an open Bedrock cancellation form after request completion.
- Changed the Bedrock form close button to red for better readability.

## [2.1.0]

- Added Java inventory GUI request handling and configurable footer buttons.
- Added `[command]` and `[close]` GUI actions.
- Added Java accept, deny and cancel controls and Floodgate response forms.

## [2.0.0 - 2.0.2]

- Updated the project for Paper 26.2.
- Added pagination, player filtering and Java/Bedrock player indicators.
- Fixed duplicate notifications, request-state cleanup and `/tpahere` direction text.
