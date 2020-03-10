package saving

import org.json.JSONArray
import org.json.JSONObject

data class DistrictDto(
    val name: String,
    var idAvito: Int = 0,
    var idCian: Int = 0,
    val isMetro: Boolean = false
) {
    fun toJSON(): JSONObject {
        return JSONObject()
            .put("name", name)
            .put("avito_id", idAvito)
            .put("cian_id", idCian)
    }
}

data class CityDto (
    val name: String,
    val avitoUrl: String,
    val avitoId: Int,
    val cianUrl: String,
    val cianId: Int,
    val districtList: ArrayList<DistrictDto> = ArrayList(),
    val metroList: ArrayList<DistrictDto> = ArrayList()
) {
    fun addDistricts(districts: List<DistrictDto>) {
        districts.forEach {
            if (it.isMetro) metroList.add(it)
            else districtList.add(it)
        }
    }

    fun toJSON(): JSONObject {
        val districtsJson = JSONArray(districtList.map(DistrictDto::toJSON))
        val metroJson = JSONArray(metroList.map(DistrictDto::toJSON))

        return JSONObject()
            .put("name", name)
            .put("avito_url", avitoUrl)
            .put("avito_id", avitoId)
            .put("cian_url", cianUrl)
            .put("cian_id", cianId)
            .put("districts", districtsJson)
            .put("metro_list", metroJson)
    }
}