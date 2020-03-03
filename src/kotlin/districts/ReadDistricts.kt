package districts

import Logger
import fetchData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import saving.DistrictDto
import saving.readCitiesJson
import saving.writeCitiesWithDistricts
import java.io.FileNotFoundException

const val FILE_NAME = "cities"

fun main() {
    Logger.logNewRunning()
    val cities = readCitiesJson(FILE_NAME)

    for (city in cities) {
        println("Get districts for ${city.name}")
        Logger.currentCity = city.name
        try {
            val districts = getDistricts(city.avitoId, city.cianId)
            city.addDistricts(districts)
        } catch (e: JSONException) { Logger.logException(e) }

        Thread.sleep(1000)
    }

    writeCitiesWithDistricts(FILE_NAME, cities)
}

fun getDistricts(avitoCityId: Int, cianCityId: Int): List<DistrictDto> {
    var isMetro = false

    Thread.sleep(500)
    val resAvito = try {
        fetchData("https://www.avito.ru/web/1/locations/districts?locationId=$avitoCityId")
    } catch (e: FileNotFoundException) {
        isMetro = true
        fetchData("https://www.avito.ru/web/1/locations/metro?locationId=$avitoCityId")
    }
    val avitoDistricts = parseDistrictsJson(JSONArray(resAvito), "avito", isMetro)

    /*if (isMetro) {
       districts.HeadlessBrowser.getMetroCian("asd")
   } else {*/
    val resCian = fetchData("https://yaroslavl.cian.ru/api/geo/get-districts-tree/?locationId=$cianCityId")
    val cianDistricts = parseDistrictsJson(JSONArray(resCian), "cian", isMetro)

    return combineDistricts(avitoDistricts, cianDistricts)
}

fun parseDistrictsJson(array: JSONArray, cite: String, isMetro: Boolean): ArrayList<DistrictDto> {
    val districts = arrayListOf<DistrictDto>()

    for (obj in array) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")

        val district = if (cite == "cian")
            DistrictDto(name, idCian = id, isMetro = isMetro)
        else DistrictDto(name, idAvito = id, isMetro = isMetro)

        districts.add(district)
    }

    return districts
}

fun combineDistricts(avitoDistricts: List<DistrictDto>, cianDistricts: List<DistrictDto>): List<DistrictDto> {
    if (avitoDistricts.size != cianDistricts.size) {
        Logger.logNotEqualsDistrictsSize(avitoDistricts.size, cianDistricts.size)
    }

    val size = if (avitoDistricts.size > cianDistricts.size)
        avitoDistricts.size
    else cianDistricts.size

    val newList = ArrayList<DistrictDto>(size)

    for (i in 0 until size) {
        if (i < cianDistricts.size) {
            val districtFromCian = cianDistricts[i]
            val districtFromAvito =
                getDistrictByName(avitoDistricts, districtFromCian.name)

            if (districtFromAvito == null) Logger.logNotFoundDistrict(districtFromCian.name, "avito")

            districtFromAvito?.let { districtFromCian.idAvito = districtFromAvito.idAvito }
            newList.add(districtFromCian)
            continue
        }

        if (i < avitoDistricts.size) {
            val districtFromAvito = avitoDistricts[i]
            val districtFromCian = getDistrictByName(cianDistricts, districtFromAvito.name)

            if (districtFromCian == null) Logger.logNotFoundDistrict(districtFromAvito.name, "cian")

            districtFromCian?.let { districtFromAvito.idCian = districtFromCian.idCian }
            newList.add(districtFromAvito)
            continue
        }
    }

    return newList
}

fun getDistrictByName(list: List<DistrictDto>, name: String): DistrictDto? {
    for (district in list) {
        if (district.name == name) return district
    }

    return null
}