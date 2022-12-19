package mc.obliviate.rocketparkour.database.parkour;

import mc.obliviate.rocketparkour.database.Database;
import mc.obliviate.rocketparkour.parkour.AbstractParkour;

import java.util.List;
import java.util.Optional;

public interface ParkourDatabase extends Database {

    void save(AbstractParkour parkour);

    void delete(AbstractParkour parkour);

    Optional<AbstractParkour> query(String name);

    List<AbstractParkour> queryAll();

}
