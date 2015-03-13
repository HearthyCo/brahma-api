package gl.glue.brahma.util;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.MalformedURLException;
import java.net.URL;

public class RedisHelper {

    private static Config conf = null;
    private static JedisPool pool;

    static {
        conf = ConfigFactory.load();
    }

    public RedisHelper() {
        initRedis();
    }

    public void initRedis() {
        if(pool == null) {
            // Defaults
            String host = "localhost";
            int port = 6379;

            URL url = null;
            try {
                String uri = conf.getString("redis.uri").replace("tcp", "http");
                url = new URL(uri);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (url != null) {
                host = url.getHost();
                port = url.getPort();
            }

            pool = new JedisPool(new JedisPoolConfig(), host, port);
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
