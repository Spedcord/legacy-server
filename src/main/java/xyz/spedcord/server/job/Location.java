package xyz.spedcord.server.job;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class Location {

    public double x, y, z;

    @BsonCreator
    public Location(@BsonProperty("x") double x, @BsonProperty("y") double y, @BsonProperty("z") double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
