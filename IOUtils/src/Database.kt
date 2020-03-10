import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

private const val CITY_NAME_LENGTH = 30
private const val DISTRICT_NAME_LENGTH = 30
private const val URL_LENGTH = 50

object CityTable : IdTable<String>() {
    override val id: Column<EntityID<String>>
        get() = varchar("id", CITY_NAME_LENGTH).entityId()
    val name = varchar("name", CITY_NAME_LENGTH)
    val avitoId = DistrictTable.integer("avito_id")
    val avitoUrl = DistrictTable.varchar("avito_url", URL_LENGTH)
    val cianId = DistrictTable.integer("cian_id")
    val cianUrl = DistrictTable.varchar("cian-url", URL_LENGTH)
}

class City(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, City>(CityTable)

    var name by CityTable.name
    var avitoId by DistrictTable.avitoId
    var avitoUrl by DistrictTable.avitoUrl
    var cianId by DistrictTable.cianId
    var cianUrl by DistrictTable.cianUrl
}


object DistrictTable : IntIdTable() {
    val name = varchar("name", DISTRICT_NAME_LENGTH)
    // TODO add fields and change names
    val avitoId = integer("avito_id")
    val avitoUrl = varchar("avito_url", URL_LENGTH)
    val cianId = integer("cian_id")
    val cianUrl = varchar("cian-url", URL_LENGTH)
    val cityName = varchar("city_name", CITY_NAME_LENGTH)
}

class District(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<District>(DistrictTable)

    var name by DistrictTable.name
    var avitoId by DistrictTable.avitoId
    var avitoUrl by DistrictTable.avitoUrl
    var cianId by DistrictTable.cianId
    var cianUrl by DistrictTable.cianUrl
    var cityName by DistrictTable.cityName
}

fun connectDB() {
    Database.connect(
        "jdbc:postgresql://35.242.227.75:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "admin"
    )
}

fun saveDistrict(name: String, cityId: String, avitoId: Int = 0, cianId: Int = 0): District {
    return transaction {
        District.new {
            this.name = name
            this.cityName = cityId
            this.avitoId = avitoId
            this.cianId = cianId
        }
    }
}

fun saveCity(name: String): City {
    val id = transliterateCyrillicToLatin(name)
    return transaction {
        City.new(id) {
            this.name = name
        }
    }
}