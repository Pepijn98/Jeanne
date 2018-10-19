package info.kurozeropb.sophie.commands

import org.reflections.Reflections

class Registry {
    fun loadCommands() {
        Reflections("info.kurozeropb.sophie.commands")
                .getSubTypesOf(Command::class.java)
                .forEach { it.newInstance() }
    }

    companion object {
        val commands = mutableListOf<Command>()

        @Throws(Exception::class)
        fun registerCommand(cmd: Command): Boolean {
            val duplicate = commands.find { it.name == cmd.name }
            if (duplicate != null)
                throw Exception("A command with the name ${cmd.name} already exists")

            return commands.add(cmd)
        }

        fun getCommandByName(name: String): Command? = commands.find { name in it.aliases || name == it.name }
    }
}
