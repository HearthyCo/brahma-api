package utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import gl.glue.brahma.util.RedisHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class FakeRedisHelper extends RedisHelper {

    private static Config conf = null;
    private static JedisPool pool;

    static {
        conf = ConfigFactory.load();
    }

    @Override
    public Jedis getResource() {
        pool = new JedisPool(new JedisPoolConfig(),
            conf.getString("redis.host"),
            Integer.valueOf(conf.getString("redis.port")),
            Integer.valueOf(conf.getString("redis.timeout")),
            null,
            1);
        return pool.getResource();
    }

    public String clearAll() {
        if (pool == null) initRedis();
        return pool.getResource().flushAll();
    }

}
