/*
 * Copyright (c) 2023. Simone Baffelli
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package openbisio

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria
import ch.systemsx.cisd.common.spring.HttpInvokerUtils
import jakarta.mail.internet.InternetAddress
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import openbisio.models.OpenbisInstance
import java.io.File
import java.net.URL
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType as OpenbisDataType
import kotlinx.cli.*

object InternetAddressAsStringSerializer : KSerializer<InternetAddress> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InternetAddress", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: InternetAddress) {
        val string = value.toString()
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): InternetAddress {
        val string = decoder.decodeString()
        return InternetAddress(string)
    }
}

@JvmInline
@Serializable
value class DataType(private val t: OpenbisDataType)

class App {
    fun createService(path: URL): IApplicationServerApi {
        val con =
            HttpInvokerUtils.createServiceStub(
                IApplicationServerApi::class.java,
                path.toString() + IApplicationServerApi.SERVICE_URL,
                10000
            )
        return con
    }
}

fun sampleFetchConfig(): SampleFetchOptions {
    val sfo = SampleFetchOptions()
    sfo.withType()
    sfo.withProperties()
    sfo.withRegistrator()
    return sfo
}

fun collectionFetchConfig(): ExperimentFetchOptions {
    val efo = ExperimentFetchOptions()
    efo.withType()
    efo.withProperties()
    efo.withRegistrator()

    efo.withSamplesUsing(sampleFetchConfig())
    return efo
}

fun projectFetchConfig(): ProjectFetchOptions {
    val pfo = ProjectFetchOptions()
    pfo.withLeader()
    pfo.withRegistrator()
    pfo.withExperimentsUsing(collectionFetchConfig())
    pfo.withSamplesUsing(sampleFetchConfig())
    return pfo
}

fun spaceFecthConfig(): SpaceFetchOptions {
    val sfo = SpaceFetchOptions()
    sfo.withProjectsUsing(projectFetchConfig())
    sfo.withSamplesUsing(sampleFetchConfig())
    sfo.withRegistrator()
    return sfo
}

enum class Mode {
    dump,
    load
}

fun dumpInstance(service: IApplicationServerApi, token: String): String {
    val spaceSearchCriteria = SpaceSearchCriteria().withAndOperator()
    val spaceFetchConf = spaceFecthConfig()
    val spaces = service.searchSpaces(token, spaceSearchCriteria, spaceFetchConf).objects
    // Get property types
    val propertyTypeSearchCriteria = PropertyTypeSearchCriteria().withAndOperator()
    val propertyTypeFecthOptions = PropertyTypeFetchOptions()
    propertyTypeFecthOptions.withRegistrator()
    val props = service.searchPropertyTypes(token, propertyTypeSearchCriteria, propertyTypeFecthOptions).objects
    // Get object types
    val sampleTypeSearchCriteria = SampleTypeSearchCriteria().withAndOperator()
    val sampleTypeFetchOptions = SampleTypeFetchOptions()
    sampleTypeFetchOptions.withPropertyAssignments().withRegistrator()
    val sampleTypes = service.searchSampleTypes(token, sampleTypeSearchCriteria, sampleTypeFetchOptions).objects

    val spRep = OpenbisInstance(spaces, props, sampleTypes).apply(OpenbisInstance::updateCodes)
    val format = Json { prettyPrint = true }
    return format.encodeToString(spRep)
}


fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val openbisURL by (parser.argument(ArgType.String))
    val username by parser.argument(ArgType.String)
    val password by parser.argument(ArgType.String)
    val mode by parser.argument(ArgType.Choice<Mode>())
    val ioFile by parser.option(ArgType.String)
    parser.parse(args)
    val service = App().createService(URL(openbisURL))
    val token = service.login(username, password)
    val configFile = File(ioFile ?: "./test.json")
    when(mode){
        Mode.dump -> configFile.writeText(dumpInstance(service, token))
        Mode.load -> {
            val instance = Json.decodeFromString<OpenbisInstance>(configFile.readText()).create(service, token)
            println(instance)
        }
    }

    //println(spJs)
}

