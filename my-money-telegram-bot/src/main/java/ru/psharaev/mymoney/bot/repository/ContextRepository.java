package ru.psharaev.mymoney.bot.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.primitives.Longs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import ru.psharaev.mymoney.bot.context.Context;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ContextRepository {
    private static final SetParams SAVE_PARAMS = new SetParams().px(Duration.ofDays(7).toMillis());

    private final JedisPool jedisPool;
    private final JsonMapper jsonMapper;

    public void save(Context context) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] value = jsonMapper.writeValueAsBytes(context);
            jedis.set(Longs.toByteArray(context.getChatId()), value, SAVE_PARAMS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(long chatId) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(Longs.toByteArray(chatId));
        }
    }

    public Optional<Context> get(long chatId) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] value = jedis.get(Longs.toByteArray(chatId));
            if (value == null) {
                return Optional.empty();
            }
            Context context = jsonMapper.readValue(value, Context.class);
            return Optional.of(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
