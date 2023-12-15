package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import com.github.strindberg.emacsj.paste.Type.HISTORY
import com.github.strindberg.emacsj.paste.Type.PREFIX
import com.github.strindberg.emacsj.paste.Type.STANDARD
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCopyPasteHelper
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange

enum class Type { STANDARD, PREFIX, HISTORY }

class PasteHandler(val type: Type) : EditorWriteActionHandler() {

    companion object {
        var historyListSize = 0
        var historyListPos = 0
        var pasteType = STANDARD
    }

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val allContents = filteredContents()
        when (type) {
            STANDARD, PREFIX -> {
                historyListSize = allContents.size
                historyListPos = 0
                pasteType = type
                editor.pasteAndMove(allContents)
                editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            }
            HISTORY -> {
                editor.getUserData(EditorEx.LAST_PASTED_REGION)?.let { region ->
                    if (allContents.size == historyListSize) {
                        if ((pasteType == STANDARD && editor.caretModel.primaryCaret.offset == region.endOffset) ||
                            (pasteType == PREFIX && editor.caretModel.primaryCaret.offset == region.startOffset)
                        ) {
                            editor.document.deleteString(region.startOffset, region.endOffset)
                            editor.pasteAndMove(allContents)
                        }
                    }
                }
            }
        }
    }

    private fun Editor.pasteAndMove(allContents: List<Transferable>) {
        clipboardContents(allContents)?.let { contents ->
            pasteTransferable(contents)?.let { range ->
                putUserData(EditorEx.LAST_PASTED_REGION, range)
                caretModel.primaryCaret.moveToOffset(if (pasteType == STANDARD) range.endOffset else range.startOffset)
            }
        }
    }

    private fun clipboardContents(allContents: List<Transferable>): Transferable? {
        return allContents.takeUnless { it.isEmpty() }?.let { history ->
            history[historyListPos++ % history.size]
        }
    }

    private fun filteredContents(): List<Transferable> =
        CopyPasteManager.getInstance().allContents
            .filter {
                it.isDataFlavorSupported(DataFlavor.stringFlavor) &&
                    (it.getTransferData(DataFlavor.stringFlavor) as String).isNotBlank()
            }
            .distinctBy { it.getTransferData(DataFlavor.stringFlavor) as String }

    private fun Editor.pasteTransferable(contents: Transferable): TextRange? =
        EditorCopyPasteHelper.getInstance().pasteTransferable(this, contents)?.takeUnless { it.isEmpty() }?.get(0)
}
