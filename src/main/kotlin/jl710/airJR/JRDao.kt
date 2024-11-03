package jl710.airJR

import org.bukkit.Bukkit
import org.bukkit.Location
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class JRDao(path: String) {
    private val path = "jdbc:sqlite:$path"

    init {
        val connection = connect()
        val statement = connection.createStatement()
        statement.execute("CREATE TABLE IF NOT EXISTS jump_and_runs (" +
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
    }

    private fun connect(): Connection {
        return DriverManager.getConnection(path)!!
    }

    fun createJr(location1: Location, location2: Location, name: String): Jr {
        if (location1.world.uid != location2.world.uid) {
            throw IllegalArgumentException("location1 and location2 are in different worlds")
        }

        val connection = connect()
        val statement = connection.prepareStatement(
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
                    Location(world, results.getDouble(3), results.getDouble(4), results.getDouble(5)),
                    Location(world, results.getDouble(6), results.getDouble(7), results.getDouble(8)),
                    results.getString(3),
                )
            )
        }

        return jrs
    }

    /**
     * @return returns true if one item was deleted else false
     * */
    fun deleteJrs(jr: Jr): Boolean {
        val connection = connect()
        val preparedStatement = connection.prepareStatement("DELETE FROM jump_and_runs WHERE id=?")
        preparedStatement.setInt(1, jr.id)
        return preparedStatement.executeUpdate() == 1
    }
}
