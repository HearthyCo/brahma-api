package gl.glue.brahma.util;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisHelper {

    private static Config conf = null;
    private static JedisPool pool;

    static {
        conf = ConfigFactory.load();
    }

    public RedisHelper() {
        if(pool == null) {
            pool = new JedisPool(new JedisPoolConfig(), conf.getString("redis.server"));
        }
    }

    public Jedis getResource() {
        if(pool == null) return null;

        return pool.getResource();
    }

    public String generateKey(int id) {
        return conf.getString("redis.keyPrefix") + id;
    }
}
