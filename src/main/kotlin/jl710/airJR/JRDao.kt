package jl710.airJR

import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector

class JRDao(path: String) {
    private val path = "jdbc:sqlite:$path"

    init {
        connect().use { connection ->
            val statement = connection.createStatement()
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS jump_and_runs (" +
                            "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                            "world TEXT NOT NULL," +
                            "name TEXT NOT NULL UNIQUE," +
                            "x1 INTEGER NOT NULL," +
                            "y1 INTEGER NOT NULL," +
                            "z1 INTEGER NOT NULL," +
                            "x2 INTEGER NOT NULL," +
                            "y2 INTEGER NOT NULL," +
                            "z2 INTEGER NOT NULL" +
                            ")"
            )
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS runs (" +
                            "player TEXT NOT NULL PRIMARY KEY," +
                            "already_jumped INTEGER NOT NULL," +
                            "jr_id INTEGER NOT NULL," +
                            "x1 INTEGER NOT NULL," +
                            "y1 INTEGER NOT NULL," +
                            "z1 INTEGER NOT NULL," +
                            "x2 INTEGER NOT NULL," +
                            "y2 INTEGER NOT NULL," +
                            "z2 INTEGER NOT NULL," +
                            "x3 INTEGER NOT NULL," +
                            "y3 INTEGER NOT NULL," +
                            "z3 INTEGER NOT NULL," +
                            "FOREIGN KEY(jr_id) REFERENCES jump_and_runs(id)" +
                            ")"
            )
            // FIXME: This is a simple solution but is not really good.
            // After a reload the jump and runs should go on.
            // If the server restarts or crashes that should be handled too.
            // On Plugin load the deletion of old jump and runs should be done not here.
            statement.executeUpdate("DELETE FROM runs")
            statement.execute("PRAGMA journal_mode=WAL;")
        }
    }

    private fun connect(): Connection {
        return DriverManager.getConnection(path)!!
    }

    fun createJr(location1: Location, location2: Location, name: String): Jr {
        if (location1.world.uid != location2.world.uid) {
            throw IllegalArgumentException("location1 and location2 are in different worlds")
        }

        val connection = connect()
        val statement =
                connection.prepareStatement(
                        "INSERT INTO jump_and_runs (world, name, x1, y1, z1, x2, y2, z2) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                )

        statement.setString(1, location1.world.uid.toString())
        statement.setString(2, name)
        statement.setDouble(3, location1.x)
        statement.setDouble(4, location1.y)
        statement.setDouble(5, location1.z)
        statement.setDouble(6, location2.x)
        statement.setDouble(7, location2.y)
        statement.setDouble(8, location2.z)

        if (statement.executeUpdate() != 1) {
            throw RuntimeException("DB insert of new jump and run failed")
        }

        val keys = statement.generatedKeys
        keys.next()
        val id = statement.generatedKeys.getInt(1)

        return Jr(id, location1, location2, name)
    }

    private fun getJr(id: Int): Jr? {
        val connection = connect()
        val statement =
                connection.prepareStatement(
                        "SELECT id, world, name, x1, y1, z1, x2, y2, z2 FROM jump_and_runs WHERE id=?"
                )
        statement.setInt(1, id)
        statement.execute()
        val result = statement.resultSet
        if (!result.next()) {
            return null
        }
        val world = Bukkit.getWorld(UUID.fromString(result.getString(2)))
        return Jr(
                result.getInt(1),
                Location(world, result.getDouble(4), result.getDouble(5), result.getDouble(6)),
                Location(world, result.getDouble(7), result.getDouble(8), result.getDouble(9)),
                result.getString(3)
        )
    }

    fun getJr(name: String): Jr? {
        connect().use { connection ->
            val statement =
                    connection.prepareStatement(
                            "SELECT id, world, name, x1, y1, z1, x2, y2, z2 FROM jump_and_runs WHERE name=?"
                    )
            statement.setString(1, name)
            statement.execute()
            val result = statement.resultSet
            if (!result.next()) {
                return null
            }
            val world = Bukkit.getWorld(UUID.fromString(result.getString(2)))
            val jr =
                    Jr(
                            result.getInt(1),
                            Location(
                                    world,
                                    result.getDouble(4),
                                    result.getDouble(5),
                                    result.getDouble(6)
                            ),
                            Location(
                                    world,
                                    result.getDouble(7),
                                    result.getDouble(8),
                                    result.getDouble(9)
                            ),
                            result.getString(3)
                    )
            return jr
        }
    }

    fun getJrs(): List<Jr> {
        val connection = connect()
        val statement = connection.createStatement()
        statement.execute("SELECT id, world, name, x1, y1, z1, x2, y2, z2 FROM jump_and_runs")
        val results = statement.resultSet

        val jrs: MutableList<Jr> = mutableListOf()

        while (results.next()) {
            val world = Bukkit.getWorld(UUID.fromString(results.getString(2)))
            jrs.add(
                    Jr(
                            results.getInt(1),
                            Location(
                                    world,
                                    results.getDouble(3),
                                    results.getDouble(4),
                                    results.getDouble(5)
                            ),
                            Location(
                                    world,
                                    results.getDouble(6),
                                    results.getDouble(7),
                                    results.getDouble(8)
                            ),
                            results.getString(3),
                    )
            )
        }

        return jrs
    }

    /** @return returns true if one item was deleted else false */
    fun deleteJr(jr: Jr): Boolean {
        val connection = connect()
        val preparedStatement = connection.prepareStatement("DELETE FROM jump_and_runs WHERE id=?")
        preparedStatement.setInt(1, jr.id)
        return preparedStatement.executeUpdate() == 1
    }

    fun createRun(playerId: String, blocks: List<Vector>, jrId: Int): Run {
        connect().use { connection ->
            val statement =
                    connection.prepareStatement(
                            "INSERT INTO runs (" +
                                    "player, " +
                                    "jr_id, " +
                                    "already_jumped, " +
                                    "x1, y1, z1, " +
                                    "x2, y2, z2, " +
                                    "x3, y3, z3" +
                                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    )
            statement.setString(1, playerId)
            statement.setInt(2, jrId)
            statement.setInt(3, 0)
            statement.setInt(4, blocks.first().x.toInt())
            statement.setInt(5, blocks.first().y.toInt())
            statement.setInt(6, blocks.first().z.toInt())
            statement.setInt(7, blocks[1].x.toInt())
            statement.setInt(8, blocks[1].y.toInt())
            statement.setInt(9, blocks[1].z.toInt())
            statement.setInt(10, blocks[2].x.toInt())
            statement.setInt(11, blocks[2].y.toInt())
            statement.setInt(12, blocks[2].z.toInt())
            statement.executeUpdate()
        }
        return Run(UUID.fromString(playerId), blocks.toMutableList(), 0, getJr(jrId)!!)
    }

    /** @throws RuntimeException if there was other than 1 rows for that player */
    fun deleteRun(playerId: String) {
        val connection = connect()
        val statement = connection.prepareStatement("DELETE FROM runs WHERE player=?")
        statement.setString(1, playerId)
        if (statement.executeUpdate() != 1) {
            throw RuntimeException("")
        }
    }

    fun getRuns(): List<Run> {
        val connection = connect()
        val statement = connection.createStatement()
        statement.execute(
                "SELECT player, jr_id, already_jumped, x1, y1, z1, x2, y2, z2, x3, y3, z3 FROM runs"
        )
        val results = statement.resultSet

        val runs = mutableListOf<Run>()

        while (results.next()) {
            runs.add(
                    Run(
                            UUID.fromString(results.getString(1)),
                            mutableListOf(
                                    Vector(
                                            results.getDouble(4),
                                            results.getDouble(5),
                                            results.getDouble(6)
                                    ),
                                    Vector(
                                            results.getDouble(7),
                                            results.getDouble(8),
                                            results.getDouble(9)
                                    ),
                                    Vector(
                                            results.getDouble(10),
                                            results.getDouble(11),
                                            results.getDouble(12)
                                    )
                            ),
                            results.getInt(3),
                            getJr(results.getInt(2))!!
                    )
            )
        }

        return runs
    }

    fun getPlayerRun(playerId: String): Run? {
        val connection = connect()
        val statement =
                connection.prepareStatement(
                        "SELECT player, jr_id, already_jumped, x1, y1, z1, x2, y2, z2, x3, y3, z3 FROM runs WHERE player=?"
                )
        statement.setString(1, playerId)
        statement.execute()

        val results = statement.resultSet
        if (!results.next()) {
            return null
        }

        return Run(
                UUID.fromString(results.getString(1)),
                mutableListOf(
                        Vector(results.getDouble(4), results.getDouble(5), results.getDouble(6)),
                        Vector(results.getDouble(7), results.getDouble(8), results.getDouble(9)),
                        Vector(results.getDouble(10), results.getDouble(11), results.getDouble(12))
                ),
                results.getInt(3),
                getJr(results.getInt(2))!!
        )
    }

    fun updateRun(run: Run) {
        connect().use { connection ->
            val statement =
                    connection.prepareStatement(
                            "UPDATE runs SET player=?, jr_id=?, already_jumped=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=?, x3=?, y3=?, z3=? WHERE player=?"
                    )
            statement.setString(1, run.player.toString())
            statement.setInt(2, run.jr.id)
            statement.setInt(3, run.alreadyJumped)
            statement.setDouble(4, run.blocks[0].x)
            statement.setDouble(5, run.blocks[0].y)
            statement.setDouble(6, run.blocks[0].z)
            statement.setDouble(7, run.blocks[1].x)
            statement.setDouble(8, run.blocks[1].y)
            statement.setDouble(9, run.blocks[1].z)
            statement.setDouble(10, run.blocks[2].x)
            statement.setDouble(11, run.blocks[2].y)
            statement.setDouble(12, run.blocks[2].z)
            statement.setString(13, run.player.toString())
            if (statement.executeUpdate() != 1) {
                throw RuntimeException("Could not find the run to update")
            }
        }
    }
}
