package saving

import org.json.JSONArray
import org.json.JSONObject
import transliterateCyrillicToLatin
import java.io.File

/**
 * Read city names from .csv file
 * File put into resources package
 * Raw format: {number},{name} (1, Москва)
 *
 * @param fileName
 * @return list of city names
 */
fun readCities(fileName: String): List<String> {
    val file = {}.javaClass.getResource("$fileName.csv")
    val fileText = file.readText()

    return fileText.lines().map { it.substringAfter(",").trim() }
}

/**
 * Write cities to .json file
 * Output file structure is json object with array of cities by key 'cities'
 * File is in the resources folder
 *
 * @param fileName
 * @param jsonArray JSONArray of cities
 */
fun writeCities(fileName: String, jsonArray: JSONArray) {
    val path = pathToResourceFolder() + "$fileName.json"
    val jsonFile = File(path)

    if (jsonFile.exists()) jsonFile.delete()
    jsonFile.createNewFile()

    val jsonObject = JSONObject()
    jsonObject.put("cities", jsonArray)

    jsonFile.appendText(jsonObject.toString(2))
}


fun writeCityWithDistricts(
    fileName: String,
    cityName: String,
    districts: List<DistrictDto>
) {
    val listDistrictJson = districts.map { it.toJSON() }
    val id = transliterateCyrillicToLatin(cityName)

    val json = JSONObject()
        .put("id", id)
        .put("name", cityName)
        .put("districts", JSONArray(listDistrictJson))

    File(pathToResourceFolder() + "$fileName.json").appendText("$json \n\n")
}

fun pathToResourceFolder() = System.getProperty("user.dir") + "\\src\\resources\\"