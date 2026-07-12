[CENTER][SIZE=7][B]TPA Extension[/B][/SIZE]
[SIZE=4]Java Inventory GUI and Native Bedrock Forms for Essentials Teleport Requests[/SIZE][/CENTER]

[COLOR=#ff4d4d][B]Language notice:[/B][/COLOR] The SpigotMC resource page, documentation and support channel are English-only. Chinese-language support is not provided on SpigotMC.

[SIZE=5][B]About TPA Extension[/B][/SIZE]
TPA Extension is a lightweight user-interface layer for Essentials teleport requests on Paper 26.2. Java Edition players receive a chest-style online-player selector and clickable chat actions. Bedrock Edition players connected through Floodgate receive native player-selection, response and cancellation forms.

Essentials remains responsible for permissions, cooldowns, teleport delays, ignored players and the final teleport. TPA Extension does not replace or bypass the Essentials teleport system.

[SIZE=5][B]Compatibility[/B][/SIZE]
[LIST]
[*][B]Server software:[/B] Paper 26.2 only
[*][B]Plugin bytecode:[/B] Java 21; run the Java version required by your Paper 26.2 server
[*][B]Required dependency:[/B] EssentialsX, registered as [ICODE]Essentials[/ICODE]
[*][B]Optional dependency:[/B] Floodgate 2.x for native Bedrock forms
[*][B]Optional integration:[/B] QuickMenu is not required; only the default configurable back-button command references it
[*][B]Server testing:[/B] Java and Bedrock request flows were successfully tested on a live Paper 26.2 server
[*]Spigot server compatibility has not been tested and is not claimed
[/LIST]

[SIZE=5][B]Free Resource[/B][/SIZE]
TPA Extension is published as a [B]free resource at US$0.00[/B]. It is a focused open-source integration released under the MIT License, has no activation system or paid service, and its source code and release binaries are publicly available on GitHub. Free distribution matches the project's scope and keeps installation accessible to Paper server owners.

[SIZE=5][B]Main Features[/B][/SIZE]
[LIST]
[*]54-slot Java inventory GUI with 45 player slots
[*]Online-player heads with Java or Bedrock edition indicators
[*]Player-name filtering, pagination and list refresh
[*]Configurable Java GUI footer buttons, materials, names, lore and actions
[*]Native Floodgate player-selection and search forms for Bedrock players
[*]Clickable Java accept, deny and cancel actions
[*]Native Bedrock accept, deny and cancel forms
[*]Automatic closing of the Bedrock sender's cancellation form after acceptance, denial or tracked-request expiry
[*]Enhancement of manually entered Essentials [ICODE]/tpa[/ICODE] and [ICODE]/tpahere[/ICODE] requests
[*]Independent in-memory tracking for concurrent requester/target pairs
[*]Automatic cleanup when a player disconnects or a tracked request expires
[/LIST]

[SIZE=5][B]Teleport Authority and Security[/B][/SIZE]
All teleport commands run as the player who selected the action. TPA Extension never grants permissions, executes the teleport as console, or bypasses Essentials restrictions. Essentials remains the authority for command permission checks, cooldowns, request blocking, teleport warm-up and final movement.

[SIZE=5][B]Request State and Privacy[/B][/SIZE]
Tracked request state contains only requester UUID, target UUID, request type and creation time. It is held in server memory for the active request, is removed on response, cancellation, disconnect or expiry, and is never written to disk. The plugin has no analytics, network telemetry, account system or database.

[SIZE=5][B]Economy and Transactions[/B][/SIZE]
TPA Extension has no economy, shop, payment, trade or item-transfer feature. It does not integrate with Vault and never handles currency or player inventory transactions.

[SIZE=5][B]Dependencies[/B][/SIZE]
[LIST]
[*][B]EssentialsX:[/B] required. The plugin disables itself if [ICODE]Essentials[/ICODE] is unavailable.
[*][B]Floodgate:[/B] optional. Without it, Java functionality remains available but native Bedrock forms are disabled.
[*][B]Geyser:[/B] normally used with Floodgate to connect Bedrock clients; it is not declared as a direct plugin dependency.
[*][B]QuickMenu:[/B] optional. Replace the default [ICODE]quickmenu open[/ICODE] back-button action if your server uses another menu plugin.
[/LIST]

[SIZE=5][B]Links[/B][/SIZE]
[LIST]
[*][URL=https://github.com/yangzijian52/TPA-Extension]Source Code and README[/URL]
[*][URL=https://github.com/yangzijian52/TPA-Extension/releases]GitHub Releases[/URL]
[*][URL=https://github.com/yangzijian52/TPA-Extension/issues]Bug Reports and Support[/URL]
[*][URL=https://github.com/yangzijian52/TPA-Extension/blob/main/docs/SPIGOTMC-RESOURCE-BBCODE.txt]Complete Documentation[/URL]
[/LIST]

[SIZE=5][B]Important Notes[/B][/SIZE]
[LIST]
[*]The GUI player selector sends [ICODE]/tpa[/ICODE]. Manual [ICODE]/tpahere[/ICODE] commands receive the additional response UI, but the GUI does not provide a TPAHere selector.
[*]The plugin adds UI around Essentials messages; it does not suppress Essentials chat output.
[*]Only canonical [ICODE]/tpa[/ICODE], [ICODE]/tpahere[/ICODE], [ICODE]/tpaccept[/ICODE], [ICODE]/tpdeny[/ICODE] and [ICODE]/tpacancel[/ICODE] command names are observed for manual-command UI tracking.
[*]The plugin tracks requests for approximately 30 seconds for UI cleanup. Essentials may use its own separately configured request duration.
[*]Do not use Bukkit [ICODE]/reload[/ICODE]. Restart the server after plugin or configuration changes.
[*]Existing server [ICODE]config.yml[/ICODE] files are not overwritten during upgrades.
[*]The SpigotMC resource page, documentation and support channel are English-only. Chinese-language support is not provided on SpigotMC.
[/LIST]
