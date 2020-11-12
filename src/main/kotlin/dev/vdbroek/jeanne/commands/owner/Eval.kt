package dev.vdbroek.jeanne.commands.owner

import dev.vdbroek.jeanne.Jeanne
import dev.vdbroek.jeanne.commands.Command
import dev.vdbroek.jeanne.core.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

class Eval : Command(
    name = "eval",
    category = Category.OWNER,
    description = "Run javascript code",
    isDeveloperOnly = true,
    isHidden = true,
    botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
    private val scriptEngine = ScriptEngineManager().getEngineByExtension("js")

    override suspend fun execute(args: List<String>, e: MessageReceivedEvent) {
        Utils.catchAll("Exception occured in eval command", e.channel) {
            if (args.isEmpty())
                return e.reply("Script can not be empty.")

            val script = args.joinToString(" ")
            val variables = hashMapOf<String, Any>().apply {
                put("ctx", e)
                put("Jeanne", Jeanne)
                put("Utils", Utils)
                put("e", Utils(e))
            }

            val scope = SimpleScriptContext().apply {
                getBindings(ScriptContext.ENGINE_SCOPE).apply {
                    variables.forEach { (k, o) -> this[k] = o }
                }
            }

            val result = scriptEngine.eval(script, scope)
            if (result != null)
                e.reply(result.toString())
            else
                e.message.addReaction(e.jda.getEmoteById("377989994461134848")!!).queue()
        }
    }
}