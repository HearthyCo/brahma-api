package gl.glue.brahma.util;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.Logger;
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
            pool = new JedisPool(
                    new JedisPoolConfig(),
                    conf.getString("redis.host"),
                    Integer.valueOf(conf.getString("redis.port")),
                    Integer.valueOf(conf.getString("redis.timeout")),
                    null,
                    Integer.valueOf(conf.getString("redis.database")));
        }

        Logger.info("INIT REDIS " + conf.getString("redis.database"));
    }

    public Jedis getResource() {
        if(pool == null) return null;

        return pool.getResource();
    }

    public String generateKey(int id) {
        return conf.getString("redis.keyPrefix") + id;
    }
}
