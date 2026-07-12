# SpigotMC Manual Publishing Guide

## Release decision

| Field | Value |
| --- | --- |
| Resource type | **Free** |
| Price | **US$0.00** |
| License | **MIT License** |
| Upload file | `target/tpa-extension-2.1.2.jar` |
| GitHub release | <https://github.com/yangzijian52/TPA-Extension/releases/tag/v2.1.2> |
| Source code | <https://github.com/yangzijian52/TPA-Extension> |
| Resource-page BBCode | `docs/SPIGOTMC-RESOURCE.md` |
| Full-documentation BBCode | `docs/SPIGOTMC-RESOURCE-BBCODE.txt` |

TPA Extension should be a free resource because it is a focused open-source integration, is distributed under the MIT License, has public source and binaries, and contains no paid service, activation system or exclusive premium component. A free listing matches the existing distribution model and lowers the installation barrier for Paper server owners.

## Suggested SpigotMC fields

| SpigotMC field | Suggested value |
| --- | --- |
| Resource title | `TPA Extension` |
| Tag line | `Java GUI and native Bedrock forms for Essentials teleport requests` |
| Version | `2.1.2` |
| Category | The closest available teleportation, administration, utilities or miscellaneous plugin category |
| Supported platform | `Paper 26.2 only` |
| Source URL | `https://github.com/yangzijian52/TPA-Extension` |
| Support URL | `https://github.com/yangzijian52/TPA-Extension/issues` |
| Download method | Upload `target/tpa-extension-2.1.2.jar` directly |
| External download | GitHub Release may be supplied only if the SpigotMC form permits it; prefer the direct jar upload |
| License | `MIT` |
| Price | `Free / US$0.00` |

For any Minecraft-version selector, choose only the version represented by the Paper 26.2 server used for testing. Do not select additional versions merely to increase visibility.

## Important warnings

- The SpigotMC resource page, documentation and support channel are English-only. Chinese-language support is not provided on SpigotMC.
- Do not claim compatibility with Spigot, Bukkit forks or Paper versions that were not tested.
- EssentialsX is required. Floodgate is optional but required for native Bedrock forms.
- The default Java GUI back button runs `quickmenu open`; QuickMenu itself is not a dependency and server owners may change this command.
- Upload the final shaded `target/tpa-extension-2.1.2.jar`, not `original-tpa-extension-2.1.2.jar`, a ZIP archive, an IDE output file or source directory.
- Do not upload `.idea`, `*.iml`, `target`, local Maven caches or temporary publishing files to GitHub.
- The plugin does not suppress Essentials messages, has no persistent player database and has no economy or transaction feature.
- The GUI selector sends `/tpa`; reverse-direction `/tpahere` requests are enhanced when entered manually.
- Review the final SpigotMC preview before submitting. BBCode rendering can differ from GitHub Markdown rendering.

## Manual publish steps

1. Open the GitHub Release and verify that `v2.1.2` is published, is not a draft or prerelease, and contains `tpa-extension-2.1.2.jar`.
2. Verify the local jar SHA-256 against the value stated in the GitHub Release notes.
3. Sign in to SpigotMC and create a new resource.
4. Select a **free** resource and set the price to **US$0.00**.
5. Enter the suggested title, tag line, version, category, platform, source and support fields above.
6. Open `docs/SPIGOTMC-RESOURCE.md`, copy all BBCode and paste it into the main resource-description editor.
7. Open `docs/SPIGOTMC-RESOURCE-BBCODE.txt`, copy all BBCode and paste it into the complete documentation or additional information area available in the form.
8. Upload `target/tpa-extension-2.1.2.jar` as the resource file.
9. Confirm that the uploaded filename, version and dependency statements are correct.
10. Preview the page and verify headings, lists, code blocks, URLs and the English-only support notice.
11. Submit the resource manually and complete any SpigotMC moderation or verification prompts.
12. After approval, add the final SpigotMC resource URL to the GitHub README and future release notes.
