package districts

import CityDto
import DistrictDto
import Site
import fetchData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import readCitiesJson
import writeCitiesWithDistricts
import java.io.FileNotFoundException

const val FILE_NAME = "test"

internal fun main() {
    Logger.logNewRunning()
    val cities = readCitiesJson(FILE_NAME)

    Browser().use {
        for (city in cities) {
            println("Get districts for ${city.name}")
            Logger.currentCity = city.name
            try {
                val districts = getDistricts(city, browser = it)
                city.addDistricts(districts)
            } catch (e: JSONException) {
                Logger.logException(e)
            }

            Thread.sleep(1000)
        }
    }

    writeCitiesWithDistricts(FILE_NAME, cities)
}

internal fun getDistricts(city: CityDto, browser: Browser): List<DistrictDto> {
    var isMetro = false

    Thread.sleep(500)
    val avitoDistricts = try {
        val resAvito = try {
            fetchData("https://www.avito.ru/web/1/locations/districts?locationId=${city.avitoId}")
        } catch (e: FileNotFoundException) {
            isMetro = true
            fetchData("https://www.avito.ru/web/1/locations/metro?locationId=${city.avitoId}")
        }

        parseDistrictsJson(JSONArray(resAvito), Site.AVITO, isMetro)
    } catch (e: Exception) {
        e.printStackTrace();
        emptyList<DistrictDto>()
    }

    // TODO govno code
    val cianDistricts = try {
        if (isMetro) {
            browser.getMetroCian(city)
        } else {
            // TODO достаточно ли id
            val resCian = fetchData("https://yaroslavl.cian.ru/api/geo/get-districts-tree/?locationId=${city.cianId}")
            parseDistrictsJson(JSONArray(resCian), Site.CIAN, false)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList<DistrictDto>()
    }

    return combineDistricts(avitoDistricts, cianDistricts)
}

internal fun parseDistrictsJson(array: JSONArray, site: Site, isMetro: Boolean): ArrayList<DistrictDto> {
    val districts = arrayListOf<DistrictDto>()

    for (obj in array) {
        if (obj !is JSONObject) continue

        val id = obj.getInt("id")
        val name = obj.getString("name")

        val district = if (site == Site.CIAN)
            DistrictDto(name, idCian = id, isMetro = isMetro)
        else DistrictDto(name, idAvito = id, isMetro = isMetro)

        districts.add(district)
    }

    return districts
}

// TODO test empty list and not empty
internal fun combineDistricts(avitoDistricts: List<DistrictDto>, cianDistricts: List<DistrictDto>): List<DistrictDto> {
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

internal fun getDistrictByName(list: List<DistrictDto>, name: String): DistrictDto? {
    for (district in list) {
        if (district.name == name) return district
    }

    return null
}