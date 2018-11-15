package info.kurozeropb.sophie.commands.owner

import info.kurozeropb.sophie.Sophie
import info.kurozeropb.sophie.commands.Command
import info.kurozeropb.sophie.core.Utils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import javax.script.*

class Eval : Command(
        name = "eval",
        category = Category.OWNER,
        description = "Run javascript code",
        isDeveloperOnly = true,
        isHidden = true,
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
    private val scriptEngine = ScriptEngineManager().getEngineByName("javascript")

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in eval command", e.channel) {
            if (args.isEmpty())
                return e.reply("Script can not be empty.")

            val script = args.joinToString(" ")
            val variables = hashMapOf<String, Any>().apply {
                put("ctx", e)
                put("Sophie", Sophie)
                put("Utils", Utils)
                put("e", Utils(e))
            }

            val scope = SimpleScriptContext().apply {
                getBindings(ScriptContext.ENGINE_SCOPE).apply {
                    variables.forEach { k, o -> this[k] = o }
                }
            }

            val result = scriptEngine.eval(script, scope)
            if (result != null)
                e.reply(result.toString())
            else
                e.message.addReaction(e.jda.getEmoteById("377989994461134848")).queue()
        }
    }
}