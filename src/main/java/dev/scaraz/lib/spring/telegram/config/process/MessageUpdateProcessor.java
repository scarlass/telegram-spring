package dev.scaraz.lib.spring.telegram.config.process;


import dev.scaraz.lib.spring.telegram.bind.TelegramCmdMessage;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandler;
import dev.scaraz.lib.spring.telegram.bind.TelegramHandlerExecutor;
import dev.scaraz.lib.spring.telegram.bind.enums.ChatSource;
import dev.scaraz.lib.spring.telegram.bind.enums.UpdateType;
import dev.scaraz.lib.spring.telegram.config.TelegramContext;
import dev.scaraz.lib.spring.telegram.config.TelegramHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Slf4j
@Component
@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
public class MessageUpdateProcessor extends UpdateProcessor {

    private final TelegramHandlerRegistry registry;
    private final AntPathMatcher pathMatcher = new AntPathMatcher("/");

    private Optional<TelegramHandler> findHandler(TelegramCmdMessage cmdMessage) {
        log.trace("Find command - {}", cmdMessage);
        if (cmdMessage.isCommand()) {
            String cmd = cmdMessage.getCommand();
            for (String path : registry.getCommandHandlers().keySet()) {
                boolean match = pathMatcher.match(path, cmd);
                log.trace("[{}] Pattern matcher {} - match ? {}", cmd, path, match);
                if (match)
                    return Optional.ofNullable(registry.getCommandHandlers().get(path));
            }
        }

        log.debug("using default message handler - if any");
        return Optional.ofNullable(registry.getDefaultMessageHandler());
    }

    @Override
    public void additionalHandlerProcess(TelegramContext context, TelegramHandlerExecutor executor) {
        executor.setCommand(new TelegramCmdMessage(context.getUpdate()));
    }

    @Override
    public UpdateType type() {
        return UpdateType.MESSAGE;
    }

    @Override
    public Optional<TelegramHandler> getHandler(Update update) {
        TelegramCmdMessage cmd = new TelegramCmdMessage(update);
        return findHandler(cmd);
    }

    @Override
    public Long getChatId(Update update) {
        return update.getMessage().getChatId();
    }

    @Override
    public ChatSource getChatSource(Update update) {
        return ChatSource.fromType(update.getMessage().getChat().getType());
    }

    @Override
    public User getUserFrom(Update update) {
        return update.getMessage().getFrom();
    }

    public Message getMessage(Update update) {
        return update.getMessage();
    }

}
