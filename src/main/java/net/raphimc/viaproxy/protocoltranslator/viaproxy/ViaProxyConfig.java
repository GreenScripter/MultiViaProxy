/*
 * This file is part of ViaProxy - https://github.com/RaphiMC/ViaProxy
 * Copyright (C) 2021-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.raphimc.viaproxy.protocoltranslator.viaproxy;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.util.Config;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.raphimc.vialoader.util.JLoggerToSLF4J;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.cli.BetterHelpFormatter;
import net.raphimc.viaproxy.cli.HelpRequestedException;
import net.raphimc.viaproxy.cli.ProtocolVersionConverter;
import net.raphimc.viaproxy.plugins.events.PostOptionsParseEvent;
import net.raphimc.viaproxy.plugins.events.PreOptionsParseEvent;
import net.raphimc.viaproxy.protocoltranslator.ProtocolTranslator;
import net.raphimc.viaproxy.saves.impl.accounts.Account;
import net.raphimc.viaproxy.saves.impl.accounts.MicrosoftAccount;
import net.raphimc.viaproxy.util.AddressUtil;
import net.raphimc.viaproxy.util.logging.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViaProxyConfig extends Config implements com.viaversion.viaversion.api.configuration.Config {

    private static final java.util.logging.Logger LOGGER = new JLoggerToSLF4J(LoggerFactory.getLogger("ViaProxy"));

    private final OptionParser optionParser;
    private final OptionSpec<Void> optionHelp;
    private final OptionSpec<String> optionBindAddress;
    private final OptionSpec<String> optionTargetAddress;
    private final OptionSpec<ProtocolVersion> optionTargetVersion;
    private final OptionSpec<Boolean> optionProxyOnlineMode;
    private final OptionSpec<AuthMethod> optionAuthMethod;
    private final OptionSpec<Integer> optionMinecraftAccountIndex;
    private final OptionSpec<Boolean> optionBetacraftAuth;
    private final OptionSpec<String> optionBackendProxyUrl;
    private final OptionSpec<Boolean> optionBackendHaProxy;
    private final OptionSpec<Boolean> optionFrontendHaProxy;
    private final OptionSpec<Boolean> optionChatSigning;
    private final OptionSpec<Integer> optionCompressionThreshold;
    private final OptionSpec<Boolean> optionAllowBetaPinging;
    private final OptionSpec<Boolean> optionIgnoreProtocolTranslationErrors;
    private final OptionSpec<Boolean> optionSuppressClientProtocolErrors;
    private final OptionSpec<Boolean> optionAllowLegacyClientPassthrough;
    private final OptionSpec<Boolean> optionBungeecordPlayerInfoPassthrough;
    private final OptionSpec<String> optionCustomMotd;
    private final OptionSpec<String> optionResourcePackUrl;
    private final OptionSpec<WildcardDomainHandling> optionWildcardDomainHandling;
    private final OptionSpec<Boolean> optionSimpleVoiceChatSupport;
    private final OptionSpec<Boolean> optionAutomaticTokenRefresh;
    private final OptionSpec<Boolean> optionFakeAcceptResourcePacks;

    private SocketAddress bindAddress = AddressUtil.parse("0.0.0.0:25568", null);
    private SocketAddress targetAddress = AddressUtil.parse("127.0.0.1:25565", null);
    private ProtocolVersion targetVersion = ProtocolTranslator.AUTO_DETECT_PROTOCOL;
    private boolean proxyOnlineMode = false;
    private AuthMethod authMethod = AuthMethod.NONE;
    private Account account = null;
    private boolean betacraftAuth = false;
    private URI backendProxyUrl = null;
    private boolean backendHaProxy = false;
    private boolean frontendHaProxy = false;
    private boolean chatSigning = true;
    private int compressionThreshold = 256;
    private boolean allowBetaPinging = false;
    private boolean ignoreProtocolTranslationErrors = false;
    private boolean suppressClientProtocolErrors = false;
    private boolean allowLegacyClientPassthrough = false;
    private boolean bungeecordPlayerInfoPassthrough = false;
    private String customMotd = "";
    private String resourcePackUrl = "";
    private WildcardDomainHandling wildcardDomainHandling = WildcardDomainHandling.NONE;
    private boolean simpleVoiceChatSupport = false;
    private boolean automaticTokenRefresh = false;
    private boolean fakeAcceptResourcePacks = false;

    public ViaProxyConfig(final File configFile) {
        super(configFile, LOGGER);

        this.optionParser = new OptionParser();
        this.optionHelp = this.optionParser.accepts("help").forHelp();
        this.optionBindAddress = this.optionParser.accepts("bind-address").withRequiredArg().ofType(String.class).defaultsTo(AddressUtil.toString(this.bindAddress));
        this.optionTargetAddress = this.optionParser.accepts("target-address").withRequiredArg().ofType(String.class).defaultsTo(AddressUtil.toString(this.targetAddress));
        this.optionTargetVersion = this.optionParser.accepts("target-version").withRequiredArg().withValuesConvertedBy(new ProtocolVersionConverter()).defaultsTo(this.targetVersion);
        this.optionProxyOnlineMode = this.optionParser.accepts("proxy-online-mode").withRequiredArg().ofType(Boolean.class).defaultsTo(this.proxyOnlineMode);
        this.optionAuthMethod = this.optionParser.accepts("auth-method").withRequiredArg().ofType(AuthMethod.class).defaultsTo(this.authMethod);
        this.optionMinecraftAccountIndex = this.optionParser.accepts("minecraft-account-index").withRequiredArg().ofType(Integer.class).defaultsTo(0);
        this.optionBetacraftAuth = this.optionParser.accepts("betacraft-auth").withRequiredArg().ofType(Boolean.class).defaultsTo(this.betacraftAuth);
        this.optionBackendProxyUrl = this.optionParser.accepts("backend-proxy-url").withRequiredArg().ofType(String.class).defaultsTo("");
        this.optionBackendHaProxy = this.optionParser.accepts("backend-haproxy").withRequiredArg().ofType(Boolean.class).defaultsTo(this.backendHaProxy);
        this.optionFrontendHaProxy = this.optionParser.accepts("frontend-haproxy").withRequiredArg().ofType(Boolean.class).defaultsTo(this.frontendHaProxy);
        this.optionChatSigning = this.optionParser.accepts("chat-signing").withRequiredArg().ofType(Boolean.class).defaultsTo(this.chatSigning);
        this.optionCompressionThreshold = this.optionParser.accepts("compression-threshold").withRequiredArg().ofType(Integer.class).defaultsTo(this.compressionThreshold);
        this.optionAllowBetaPinging = this.optionParser.accepts("allow-beta-pinging").withRequiredArg().ofType(Boolean.class).defaultsTo(this.allowBetaPinging);
        this.optionIgnoreProtocolTranslationErrors = this.optionParser.accepts("ignore-protocol-translation-errors").withRequiredArg().ofType(Boolean.class).defaultsTo(this.ignoreProtocolTranslationErrors);
        this.optionSuppressClientProtocolErrors = this.optionParser.accepts("suppress-client-protocol-errors").withRequiredArg().ofType(Boolean.class).defaultsTo(this.suppressClientProtocolErrors);
        this.optionAllowLegacyClientPassthrough = this.optionParser.accepts("allow-legacy-client-passthrough").withRequiredArg().ofType(Boolean.class).defaultsTo(this.allowLegacyClientPassthrough);
        this.optionBungeecordPlayerInfoPassthrough = this.optionParser.accepts("bungeecord-player-info-passthrough").withRequiredArg().ofType(Boolean.class).defaultsTo(this.bungeecordPlayerInfoPassthrough);
        this.optionCustomMotd = this.optionParser.accepts("custom-motd").withRequiredArg().ofType(String.class).defaultsTo(this.customMotd);
        this.optionResourcePackUrl = this.optionParser.accepts("resource-pack-url").withRequiredArg().ofType(String.class).defaultsTo(this.resourcePackUrl);
        this.optionWildcardDomainHandling = this.optionParser.accepts("wildcard-domain-handling").withRequiredArg().ofType(WildcardDomainHandling.class).defaultsTo(this.wildcardDomainHandling);
        this.optionSimpleVoiceChatSupport = this.optionParser.accepts("simple-voice-chat-support").withRequiredArg().ofType(Boolean.class).defaultsTo(this.simpleVoiceChatSupport);
        this.optionAutomaticTokenRefresh = this.optionParser.accepts("automatic-token-refresh").withRequiredArg().ofType(Boolean.class).defaultsTo(this.automaticTokenRefresh);
		Thread refresh = new Thread(() -> {
			final long delay = 10 * 60 * 1000;
			while (true) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (shouldAutomaticallyRefreshTokens()) {
					MicrosoftAccount acc = ViaProxy.getSaveManager().accountsSave.getAccounts().stream()//
							.filter(a -> a instanceof MicrosoftAccount)//
							.map(a -> (MicrosoftAccount) a)//
							.min((a1, a2) -> (int) (a1.getMcProfile().getMcToken().getExpireTimeMs() - a2.getMcProfile().getMcToken().getExpireTimeMs()))//
							.orElse(null);
					if (acc != null) {
						long targetTime = acc.getMcProfile().getMcToken().getExpireTimeMs();
						System.out.println("Next token refresh " + acc.getMcProfile().getName() + " in " + (targetTime - System.currentTimeMillis()) + " ms. (" +//
								Math.max(0, (targetTime - System.currentTimeMillis())) + " ms.)");
						if ((targetTime - System.currentTimeMillis()) < delay) {
							try {
								Thread.sleep(Math.max(0, (targetTime - System.currentTimeMillis())));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								acc.refresh();
								ViaProxy.getSaveManager().save();
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		refresh.setName("Account Token Refresh Thread");
		refresh.start();
        this.optionFakeAcceptResourcePacks = this.optionParser.accepts("fake-accept-resource-packs").withRequiredArg().ofType(Boolean.class).defaultsTo(this.fakeAcceptResourcePacks);
    }

    @Override
    public void reload() {
        super.reload();

        this.bindAddress = AddressUtil.parse(this.getString("bind-address", AddressUtil.toString(this.bindAddress)), null);
        this.targetVersion = ProtocolVersion.getClosest(this.getString("target-version", this.targetVersion.getName()));
        this.checkTargetVersion();
        this.targetAddress = AddressUtil.parse(this.getString("target-address", AddressUtil.toString(this.targetAddress)), this.targetVersion);
        this.proxyOnlineMode = this.getBoolean("proxy-online-mode", this.proxyOnlineMode);
        this.authMethod = AuthMethod.byName(this.getString("auth-method", this.authMethod.name()));
        final List<Account> accounts = ViaProxy.getSaveManager().accountsSave.getAccounts();
        final int accountIndex = this.getInt("minecraft-account-index", 0);
        if (this.authMethod == AuthMethod.ACCOUNT && accountIndex >= 0 && accountIndex < accounts.size()) {
            this.account = accounts.get(accountIndex);
        } else {
            this.account = null;
        }
        this.betacraftAuth = this.getBoolean("betacraft-auth", this.betacraftAuth);
        this.backendProxyUrl = this.parseProxyUrl(this.getString("backend-proxy-url", ""));
        this.backendHaProxy = this.getBoolean("backend-haproxy", this.backendHaProxy);
        this.frontendHaProxy = this.getBoolean("frontend-haproxy", this.frontendHaProxy);
        this.chatSigning = this.getBoolean("chat-signing", this.chatSigning);
        this.compressionThreshold = this.getInt("compression-threshold", this.compressionThreshold);
        this.allowBetaPinging = this.getBoolean("allow-beta-pinging", this.allowBetaPinging);
        this.ignoreProtocolTranslationErrors = this.getBoolean("ignore-protocol-translation-errors", this.ignoreProtocolTranslationErrors);
        this.suppressClientProtocolErrors = this.getBoolean("suppress-client-protocol-errors", this.suppressClientProtocolErrors);
        this.allowLegacyClientPassthrough = this.getBoolean("allow-legacy-client-passthrough", this.allowLegacyClientPassthrough);
        this.bungeecordPlayerInfoPassthrough = this.getBoolean("bungeecord-player-info-passthrough", this.bungeecordPlayerInfoPassthrough);
        this.customMotd = this.getString("custom-motd", this.customMotd);
        this.resourcePackUrl = this.getString("resource-pack-url", this.resourcePackUrl);
        this.wildcardDomainHandling = WildcardDomainHandling.byName(this.getString("wildcard-domain-handling", this.wildcardDomainHandling.name()));
        this.simpleVoiceChatSupport = this.getBoolean("simple-voice-chat-support", this.simpleVoiceChatSupport);
        this.automaticTokenRefresh = this.getBoolean("automatic-token-refresh", this.automaticTokenRefresh);
        this.fakeAcceptResourcePacks = this.getBoolean("fake-accept-resource-packs", this.fakeAcceptResourcePacks);
    }

    public void loadFromArguments(final String[] args) throws IOException {
        try {
            ViaProxy.EVENT_MANAGER.call(new PreOptionsParseEvent(this.optionParser));
            final OptionSet options = this.optionParser.parse(args);
            if (options.has(this.optionHelp)) {
                throw new HelpRequestedException();
            }
            this.bindAddress = AddressUtil.parse(options.valueOf(this.optionBindAddress), null);
            this.targetVersion = options.valueOf(this.optionTargetVersion);
            this.checkTargetVersion();
            this.targetAddress = AddressUtil.parse(options.valueOf(this.optionTargetAddress), this.targetVersion);
            this.proxyOnlineMode = options.valueOf(this.optionProxyOnlineMode);
            this.authMethod = options.valueOf(this.optionAuthMethod);
            final List<Account> accounts = ViaProxy.getSaveManager().accountsSave.getAccounts();
            final int accountIndex = options.valueOf(this.optionMinecraftAccountIndex);
            if (options.has(this.optionMinecraftAccountIndex) && accountIndex >= 0 && accountIndex < accounts.size()) {
                this.authMethod = AuthMethod.ACCOUNT;
                this.account = accounts.get(accountIndex);
            } else {
                this.account = null;
            }
            this.betacraftAuth = options.valueOf(this.optionBetacraftAuth);
            this.backendProxyUrl = this.parseProxyUrl(options.valueOf(this.optionBackendProxyUrl));
            this.backendHaProxy = options.valueOf(this.optionBackendHaProxy);
            this.frontendHaProxy = options.valueOf(this.optionFrontendHaProxy);
            this.chatSigning = options.valueOf(this.optionChatSigning);
            this.compressionThreshold = options.valueOf(this.optionCompressionThreshold);
            this.allowBetaPinging = options.valueOf(this.optionAllowBetaPinging);
            this.ignoreProtocolTranslationErrors = options.valueOf(this.optionIgnoreProtocolTranslationErrors);
            this.suppressClientProtocolErrors = options.valueOf(this.optionSuppressClientProtocolErrors);
            this.allowLegacyClientPassthrough = options.valueOf(this.optionAllowLegacyClientPassthrough);
            this.bungeecordPlayerInfoPassthrough = options.valueOf(this.optionBungeecordPlayerInfoPassthrough);
            this.customMotd = options.valueOf(this.optionCustomMotd);
            this.resourcePackUrl = options.valueOf(this.optionResourcePackUrl);
            this.wildcardDomainHandling = options.valueOf(this.optionWildcardDomainHandling);
            this.simpleVoiceChatSupport = options.valueOf(this.optionSimpleVoiceChatSupport);
            this.automaticTokenRefresh = options.valueOf(this.optionAutomaticTokenRefresh);
            this.fakeAcceptResourcePacks = options.valueOf(this.optionFakeAcceptResourcePacks);
            ViaProxy.EVENT_MANAGER.call(new PostOptionsParseEvent(options));
            return;
        } catch (OptionException e) {
            this.logger.severe("Error parsing CLI options: " + e.getMessage());
        } catch (HelpRequestedException ignored) {
        }

        this.optionParser.formatHelpWith(new BetterHelpFormatter());
        this.optionParser.printHelpOn(Logger.SYSOUT);
        this.logger.info("For a more detailed description of the options, please refer to the viaproxy.yml file.");
        System.exit(1);
    }

    @Override
    public URL getDefaultConfigURL() {
        return this.getClass().getClassLoader().getResource("assets/viaproxy/viaproxy.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> map) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }

    @Override
    public void set(String path, Object value) {
        super.set(path, value);
    }

    public SocketAddress getBindAddress() {
        return this.bindAddress;
    }

    public void setBindAddress(final SocketAddress bindAddress) {
        this.bindAddress = bindAddress;
        this.set("bind-address", AddressUtil.toString(bindAddress));
    }

    public SocketAddress getTargetAddress() {
        return this.targetAddress;
    }

    public void setTargetAddress(final SocketAddress targetAddress) {
        this.targetAddress = targetAddress;
        this.set("target-address", AddressUtil.toString(targetAddress));
    }

    public ProtocolVersion getTargetVersion() {
        return this.targetVersion;
    }

    public void setTargetVersion(final ProtocolVersion targetVersion) {
        this.targetVersion = targetVersion;
        this.set("target-version", targetVersion.getName());
    }

    public boolean isProxyOnlineMode() {
        return this.proxyOnlineMode;
    }

    public void setProxyOnlineMode(final boolean proxyOnlineMode) {
        this.proxyOnlineMode = proxyOnlineMode;
        this.set("proxy-online-mode", proxyOnlineMode);
    }

    public AuthMethod getAuthMethod() {
        return this.authMethod;
    }

    public void setAuthMethod(final AuthMethod authMethod) {
        this.authMethod = authMethod;
        this.set("auth-method", authMethod.name().toLowerCase(Locale.ROOT));
    }

    public Account getAccount() {
        return this.account;
    }
    
    public Account getAccountFromUUID(String uuid) {
		return ViaProxy.getSaveManager().accountsSave.getAccounts().stream().filter(a -> a.getUUID().toString().replace("-", "").equalsIgnoreCase(uuid.replace("-", ""))).findAny().orElse(null);
    }

    public void setAccount(final Account account) {
        this.account = account;
        this.set("minecraft-account-index", ViaProxy.getSaveManager().accountsSave.getAccounts().indexOf(account));
    }

    public boolean useBetacraftAuth() {
        return this.betacraftAuth;
    }

    public void setBetacraftAuth(final boolean betacraftAuth) {
        this.betacraftAuth = betacraftAuth;
        this.set("betacraft-auth", betacraftAuth);
    }

    public URI getBackendProxyUrl() {
        return this.backendProxyUrl;
    }

    public void setBackendProxyUrl(final URI backendProxyUrl) {
        this.backendProxyUrl = backendProxyUrl;
        if (backendProxyUrl != null) {
            this.set("backend-proxy-url", backendProxyUrl.toString());
        } else {
            this.set("backend-proxy-url", "");
        }
    }

    public boolean useBackendHaProxy() {
        return this.backendHaProxy;
    }

    public void setBackendHaProxy(final boolean backendHaProxy) {
        this.backendHaProxy = backendHaProxy;
        this.set("backend-haproxy", backendHaProxy);
    }

    public boolean useFrontendHaProxy() {
        return this.frontendHaProxy;
    }

    public void setFrontendHaProxy(final boolean frontendHaProxy) {
        this.frontendHaProxy = frontendHaProxy;
        this.set("frontend-haproxy", frontendHaProxy);
    }

    public boolean shouldSignChat() {
        return this.chatSigning;
    }

    public void setChatSigning(final boolean chatSigning) {
        this.chatSigning = chatSigning;
        this.set("chat-signing", chatSigning);
    }

    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    public void setCompressionThreshold(final int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
        this.set("compression-threshold", compressionThreshold);
    }

    public boolean shouldAllowBetaPinging() {
        return this.allowBetaPinging;
    }

    public void setAllowBetaPinging(final boolean allowBetaPinging) {
        this.allowBetaPinging = allowBetaPinging;
        this.set("allow-beta-pinging", allowBetaPinging);
    }

    public boolean shouldIgnoreProtocolTranslationErrors() {
        return this.ignoreProtocolTranslationErrors;
    }

    public void setIgnoreProtocolTranslationErrors(final boolean ignoreProtocolTranslationErrors) {
        this.ignoreProtocolTranslationErrors = ignoreProtocolTranslationErrors;
        this.set("ignore-protocol-translation-errors", ignoreProtocolTranslationErrors);
    }

    public boolean shouldSuppressClientProtocolErrors() {
        return this.suppressClientProtocolErrors;
    }

    public void setSuppressClientProtocolErrors(final boolean suppressClientProtocolErrors) {
        this.suppressClientProtocolErrors = suppressClientProtocolErrors;
        this.set("suppress-client-protocol-errors", suppressClientProtocolErrors);
    }

    public boolean shouldAllowLegacyClientPassthrough() {
        return this.allowLegacyClientPassthrough;
    }

    public void setAllowLegacyClientPassthrough(final boolean allowLegacyClientPassthrough) {
        this.allowLegacyClientPassthrough = allowLegacyClientPassthrough;
        this.set("allow-legacy-client-passthrough", allowLegacyClientPassthrough);
    }

    public boolean shouldPassthroughBungeecordPlayerInfo() {
        return this.bungeecordPlayerInfoPassthrough;
    }

    public void setPassthroughBungeecordPlayerInfo(final boolean bungeecordPlayerInfoPassthrough) {
        this.bungeecordPlayerInfoPassthrough = bungeecordPlayerInfoPassthrough;
        this.set("bungeecord-player-info-passthrough", bungeecordPlayerInfoPassthrough);
    }

    public String getCustomMotd() {
        return this.customMotd;
    }

    public void setCustomMotd(final String customMotd) {
        this.customMotd = customMotd;
        this.set("custom-motd", customMotd);
    }

    public String getResourcePackUrl() {
        return this.resourcePackUrl;
    }

    public void setResourcePackUrl(final String resourcePackUrl) {
        this.resourcePackUrl = resourcePackUrl;
        this.set("resource-pack-url", resourcePackUrl);
    }

    public WildcardDomainHandling getWildcardDomainHandling() {
        return this.wildcardDomainHandling;
    }

    public void setWildcardDomainHandling(final WildcardDomainHandling wildcardDomainHandling) {
        this.wildcardDomainHandling = wildcardDomainHandling;
        this.set("wildcard-domain-handling", wildcardDomainHandling.name().toLowerCase(Locale.ROOT));
    }

    public boolean shouldSupportSimpleVoiceChat() {
        return this.simpleVoiceChatSupport;
    }

    public void setSimpleVoiceChatSupport(final boolean simpleVoiceChatSupport) {
        this.simpleVoiceChatSupport = simpleVoiceChatSupport;
        this.set("simple-voice-chat-support", simpleVoiceChatSupport);
    }

    public boolean shouldAutomaticallyRefreshTokens() {
        return this.automaticTokenRefresh;
    }

    public void setAutomaticTokenRefresh(final boolean automaticTokenRefresh) {
        this.automaticTokenRefresh = automaticTokenRefresh;
        this.set("automatic-token-refresh", automaticTokenRefresh);
    }
    
    public boolean shouldFakeAcceptResourcePacks() {
        return this.fakeAcceptResourcePacks;
    }

    public void setFakeAcceptResourcePacks(final boolean fakeAcceptResourcePacks) {
        this.fakeAcceptResourcePacks = fakeAcceptResourcePacks;
        this.set("fake-accept-resource-packs", fakeAcceptResourcePacks);
    }

    private void checkTargetVersion() {
        if (this.targetVersion == null) {
            this.targetVersion = ProtocolTranslator.AUTO_DETECT_PROTOCOL;
            this.logger.info("Invalid target version: " + this.getString("target-version", "") + ". Defaulting to auto detect.");
            this.logger.info("=== Supported Protocol Versions ===");
            for (ProtocolVersion version : ProtocolVersion.getProtocols()) {
                this.logger.info(version.getName());
            }
            this.logger.info("===================================");
        }
    }

    private URI parseProxyUrl(final String proxyUrl) {
        if (!proxyUrl.isBlank()) {
            try {
                return new URI(proxyUrl);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid proxy url: " + proxyUrl + ". Proxy url format: type://address:port or type://username:password@address:port");
            }
        }

        return null;
    }

    public enum AuthMethod {

        /**
         * Use an account for joining the target server (Has to be configured in ViaProxy GUI)
         */
        ACCOUNT("tab.general.minecraft_account.option_select_account"),
        /**
         * No authentication (Offline mode)
         */
        NONE("tab.general.minecraft_account.option_no_account"),
        /**
         * Requires the OpenAuthMod client mod (https://modrinth.com/mod/openauthmod)
         */
        OPENAUTHMOD("tab.general.minecraft_account.option_openauthmod");

        private final String guiTranslationKey;

        AuthMethod(String guiTranslationKey) {
            this.guiTranslationKey = guiTranslationKey;
        }

        public static AuthMethod byName(String name) {
            for (AuthMethod mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }

            return NONE;
        }

        public String getGuiTranslationKey() {
            return this.guiTranslationKey;
        }

    }

    public enum WildcardDomainHandling {

        /**
         * No wildcard domain handling
         */
        NONE,
        /**
         * Public wildcard domain handling
         */
        PUBLIC,
        /**
         * Iternal wildcard domain handling
         */
        INTERNAL;

        public static WildcardDomainHandling byName(String name) {
            for (WildcardDomainHandling mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }

            return NONE;
        }

    }

}
