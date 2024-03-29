/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package openbisio

import kotlin.test.Test
import openbisio.models.ConcreteIdentifier

class DeserialiserTest{
    val configFile = javaClass.getResource("/test.json").readText()
    val instance = ch.empa.openbisio.readInstance(configFile)

    @Test
    fun testIdentifiers(){
        val sp = instance.getChild("YOUR_SPACE_CODE")
        assert(sp?.identifier?.identifier == "/YOUR_FIRST_PROJECT_CODE/YOUR_SPACE_CODE")

    }


    @Test
    fun testCodes(){
        println(instance.children?.get(0)?.children?.get(0))
    }
}



class IdentifierTest{

    @Test
    fun testInstanceIdentifier(){
        val id = ConcreteIdentifier.InstanceIdentifier()
        assert(id.identifier == "/")
    }


    @Test
    fun testSpaceIdentifier(){
        val path = "/SPACE"
        val components = path.split("/")
        val id = ConcreteIdentifier.SpaceIdentifier(components)
        println(id.identifier)
        assert(id.identifier == path)

    }

    @Test
    fun testProjectParent(){
        val id = ConcreteIdentifier.ProjectIdentifier(listOf("/", "A", "B"))
        val parent = id.getAncestor()
        assert(parent.identifier == ConcreteIdentifier.SpaceIdentifier(listOf("/", "A")).identifier)
    }
}