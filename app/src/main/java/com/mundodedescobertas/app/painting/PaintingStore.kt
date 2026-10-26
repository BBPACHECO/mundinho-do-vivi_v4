package com.mundodedescobertas.app.painting

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.util.UUID

class PaintingStore(private val context: Context) {
    val root = File(context.filesDir, "atelier").apply { mkdirs() }
    val imports = File(root, "imports").apply { mkdirs() }
    val creations = File(root, "creations").apply { mkdirs() }
    private val drafts = File(root, "drafts").apply { mkdirs() }
    fun importedDrawings(): List<File> = imports.listFiles()?.filter { it.extension == "png" }?.sortedByDescending { it.lastModified() }.orEmpty()
    fun savedCreations(): List<File> = creations.listFiles()?.filter { it.extension == "png" }?.sortedByDescending { it.lastModified() }.orEmpty()
    fun source(id: String): Bitmap = if (id.startsWith("import_")) decodeBounded(File(imports,"${id.removePrefix("import_")}.png")) else DrawingCatalog.render(DrawingCatalog.originals.first { it.id == id })
    fun readDraft(id: String): Bitmap? = File(drafts,"$id.png").takeIf { it.isFile }?.let { runCatching { decodeBounded(it) }.getOrNull() }
    fun lastSelection(): String = File(root,"last.txt").takeIf { it.isFile }?.readText()?.trim().orEmpty()
    fun writeDraft(id: String, bitmap: Bitmap) {
        atomicPng(File(drafts,"$id.png"),bitmap)
        File(root,"last.txt").writeText(id)
    }
    fun retainCreation(bitmap: Bitmap): File = File(creations,"pintura_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png").also { atomicPng(it,bitmap) }
    fun deleteImported(file: File) {
        require(file.parentFile?.canonicalFile == imports.canonicalFile)
        File(drafts,"import_${file.nameWithoutExtension}.png").delete();file.delete()
    }
    fun export(bitmap: Bitmap): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"Mundinho_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE,"image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/MundinhoDoVivi")
            put(MediaStore.Images.Media.IS_PENDING,1)
        }
        val uri=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values) ?: throw IOException("Não foi possível criar a imagem na galeria")
        try {
            context.contentResolver.openOutputStream(uri)?.use { if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) throw IOException("Falha ao gravar pintura") } ?: throw IOException("Galeria indisponível")
            context.contentResolver.update(uri,ContentValues().apply {put(MediaStore.Images.Media.IS_PENDING,0)},null,null)
            return uri
        } catch(e:Exception) { context.contentResolver.delete(uri,null,null);throw e }
    }
    /** Copies input first to private storage, with a 20MiB limit. PDFs render only page one. */
    fun import(uri: Uri): File {
        val temp=File(root,"incoming-${UUID.randomUUID()}")
        try {
            context.contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { output ->
                val buffer=ByteArray(8192);var total=0
                while(true) {val count=input.read(buffer);if(count<0)break;total+=count;if(total>20*1024*1024)throw IOException("Use um arquivo de até 20 MB");output.write(buffer,0,count)}
            }} ?: throw IOException("Não foi possível abrir o arquivo")
            if(temp.length()==0L) throw IOException("O arquivo está vazio")
            val header=temp.inputStream().use{input->ByteArray(5).also{input.read(it)}}
            val bitmap=if(String(header,Charsets.US_ASCII).startsWith("%PDF")) {
                ParcelFileDescriptor.open(temp,ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor -> PdfRenderer(descriptor).use { renderer ->
                    if(renderer.pageCount<1)throw IOException("PDF sem páginas")
                    renderer.openPage(0).use { page ->
                        val bmp=Bitmap.createBitmap(640,640,Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(Color.WHITE)
                        val scale=minOf(640f/page.width,640f/page.height)
                        val matrix=Matrix().apply {postScale(scale,scale);postTranslate((640-page.width*scale)/2,(640-page.height*scale)/2)}
                        page.render(bmp,null,matrix,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);bmp
                    }
                }}
            } else decodeBounded(temp)
            val dest=File(imports,"${UUID.randomUUID()}.png");atomicPng(dest,bitmap);bitmap.recycle();return dest
        } finally {temp.delete()}
    }
    companion object {
        fun decodeBounded(file: File): Bitmap {
            val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,bounds)
            if(bounds.outWidth<=0 || bounds.outHeight<=0 || bounds.outWidth.toLong()*bounds.outHeight>100_000_000)throw IOException("Use uma imagem PNG ou JPG de até 100 megapixels")
            var sample=1;while(bounds.outWidth/sample>1280 || bounds.outHeight/sample>1280)sample*=2
            val source=BitmapFactory.decodeFile(file.path,BitmapFactory.Options().apply{inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888}) ?: throw IOException("Imagem não reconhecida")
            val result=Bitmap.createBitmap(640,640,Bitmap.Config.ARGB_8888);val canvas=Canvas(result);canvas.drawColor(Color.WHITE)
            val scale=minOf(640f/source.width,640f/source.height);val w=source.width*scale;val h=source.height*scale
            canvas.drawBitmap(source,null,RectF((640-w)/2,(640-h)/2,(640+w)/2,(640+h)/2),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG));source.recycle();return result
        }
        fun atomicPng(file: File, bitmap: Bitmap) {
            val temp=File(file.parentFile,".${file.name}.${UUID.randomUUID()}.tmp")
            try {
                temp.outputStream().use { if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,it))throw IOException("Não foi possível salvar a pintura") }
                if(!temp.renameTo(file)) throw IOException("Não foi possível concluir o salvamento")
            } finally {temp.delete()}
        }
    }
}
