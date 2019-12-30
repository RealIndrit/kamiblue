package me.zeroeightsix.kami.command;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.syntax.SyntaxChunk;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.bewwawho.misc.NoKamiChat;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.zeroeightysix.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.launchwrapper.LogWrapper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command {

    protected String label;
    protected String syntax;
    protected String description;

    public final Minecraft mc = Minecraft.getMinecraft();

    protected SyntaxChunk[] syntaxChunks;

    public static Setting<String> commandPrefix = Settings.s("commandPrefix", ".");
    public static final char SECTION_SIGN = '\u00A7';

    public Command(String label, SyntaxChunk[] syntaxChunks) {
        this.label = label;
        this.syntaxChunks = syntaxChunks;
        this.description = "Descriptionless";
    }

    public static void sendChatMessage(String message) {
        if (ModuleManager.getModuleByName("NoKamiChat").isDisabled()) {
            sendRawChatMessage("&7[&a" + KamiMod.KAMI_KANJI + "&7] &r" + message);
        } else if (ModuleManager.getModuleByName("NoKamiChat").isEnabled() && NoKamiChat.playSound.getValue()) {
            Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.getPosition(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.MASTER, 0.5f, 1.2f);
        }
    }

    public static void sendErrorMessage(String message) {
        if (ModuleManager.getModuleByName("NoKamiChat").isDisabled()) {
            sendRawChatMessage("&7[&4" + KamiMod.KAMI_KANJI + "&7] &r" + message);
        } else if (ModuleManager.getModuleByName("NoKamiChat").isEnabled() && NoKamiChat.playSound.getValue()) {
            Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.getPosition(), SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.MASTER, 0.6f, 0.1f);
        }
    }

    public static void sendWarningMessage(String message) {
        if (ModuleManager.getModuleByName("NoKamiChat").isDisabled()) {
            sendRawChatMessage("&7[&6" + KamiMod.KAMI_KANJI + "&7] &r" + message);
        } else if (ModuleManager.getModuleByName("NoKamiChat").isEnabled() && NoKamiChat.playSound.getValue()) {
            Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.getPosition(), SoundEvents.BLOCK_NOTE_SNARE, SoundCategory.MASTER, 1f, 0.5f);
        }
    }

    public static void sendStringChatMessage(String[] messages) {
        sendChatMessage("");
        for (String s : messages) sendRawChatMessage(s);
    }

    public static void sendRawChatMessage(String message) {
        if (isSendable()) {
            Wrapper.getPlayer().sendMessage(new ChatMessage(message));
        } else {
            LogWrapper.info("KAMI Blue: Avoided NPE by logging to file instead of chat\n" + message);
        }
    }

    public static boolean isSendable() {
        if (Minecraft.getMinecraft().player == null) {
            return false;
        } else {
            return true;
        }
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String getCommandPrefix() {
        return commandPrefix.getValue();
    }

    public String getLabel() {
        return label;
    }

    public abstract void call(String[] args);

    public SyntaxChunk[] getSyntaxChunks() {
        return syntaxChunks;
    }

    public static class ChatMessage extends TextComponentBase {

        String text;

        public ChatMessage(String text) {

            Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher m = p.matcher(text);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String replacement = "\u00A7" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }

            m.appendTail(sb);

            this.text = sb.toString();
        }

        public String getUnformattedComponentText() {
            return text;
        }

        @Override
        public ITextComponent createCopy() {
            return new ChatMessage(text);
        }

    }

    protected SyntaxChunk getSyntaxChunk(String name) {
        for (SyntaxChunk c : syntaxChunks) {
            if (c.getType().equals(name))
                return c;
        }
        return null;
    }
}
