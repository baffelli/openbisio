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

package openbisio.models

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person
import jakarta.mail.internet.InternetAddress
import kotlinx.serialization.Serializable
import openbisio.InternetAddressAsStringSerializer

@Serializable
data class OpenbisPerson(
    val userId: String,
    val firstName: String?,
    val lastName: String?,
    @Serializable(with = InternetAddressAsStringSerializer::class) val email: InternetAddress?
) {
    constructor(
        userId: String,
        firstName: String?,
        lastName: String,
        email: String
    ) : this(userId, firstName, lastName, if (email != "") InternetAddress(email) else null)

    constructor(p: Person) : this(p.userId, p.firstName, p.lastName, p.email)
}