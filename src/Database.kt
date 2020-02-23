import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction
import org.json.JSONObject

private const val CITY_NAME_LENGTH = 30
private const val DISTRICT_NAME_LENGTH = 30

object CityTable : IdTable<String>() {
    override val id: Column<EntityID<String>>
        get() = varchar("id", CITY_NAME_LENGTH).entityId()
    val name = varchar("name", CITY_NAME_LENGTH)
}

class City(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, City>(CityTable)

    var name by CityTable.name
}


object DistrictTable : IntIdTable() {
    val name = varchar("name", DISTRICT_NAME_LENGTH)
    val idAvito = integer("id_avito")
    val idCian = integer("id_cian")
    val cityName = varchar("city_name", CITY_NAME_LENGTH)
}

class District(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<District>(DistrictTable)

    var name by DistrictTable.name
    var idAvito by DistrictTable.idAvito
    var idCian by DistrictTable.idCian
    var cityName by DistrictTable.cityName
}

data class DistrictDto(
    val name: String,
    var idAvito: Int = 0,
    var idCian: Int = 0
) {
    fun toJSON(): JSONObject? {
        return JSONObject()
            .put("name", name)
            .put("avito_id", idAvito)
            .put("cian_id", idCian)
    }
}

fun saveDistrict(name: String, cityId: String, avitoId: Int = 0, cianId: Int = 0): District {
    return transaction {
        District.new {
            this.name = name
            this.cityName = cityId
            this.idAvito = avitoId
            this.idCian = cianId
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