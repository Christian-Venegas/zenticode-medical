package com.zenticode.medical.recetas.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.zenticode.medical.recetas.dto.RecetaResponse;
import com.zenticode.medical.recetas.dto
        .RecetaResponse.MedicamentoResponse;
import com.zenticode.medical.recetas.entity.Receta.EstadoReceta;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Genera documentos PDF personalizados para recetas médicas.
 */
@Service
public class RecetaPdfService {

    private static final Color COLOR_PRINCIPAL =
            new Color(30, 102, 161);

    private static final Color COLOR_SECUNDARIO =
            new Color(230, 240, 248);

    private static final Color COLOR_BORDE =
            new Color(185, 200, 212);

    private static final Color COLOR_TEXTO_SUAVE =
            new Color(80, 90, 100);

    private static final Color COLOR_ANULADA =
            new Color(175, 35, 35);

    private static final DateTimeFormatter
            FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    private static final DateTimeFormatter
            FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
            );

    private final RecetaService recetaService;

    // Inyecta el servicio de recetas.
    public RecetaPdfService(
            final RecetaService recetaService
    ) {
        this.recetaService =
                Objects.requireNonNull(
                        recetaService,
                        "El servicio de recetas es obligatorio."
                );
    }

    // Genera el documento dentro de memoria.
    public byte[] generar(
            final Long idConsultorios,
            final Long idPacientes,
            final Long idConsultas,
            final Long idRecetas
    ) {
        final RecetaResponse receta =
                recetaService.buscarPorId(
                        idConsultorios,
                        idPacientes,
                        idConsultas,
                        idRecetas
                );

        validarRecetaParaPdf(
                receta
        );

        final ByteArrayOutputStream salida =
                new ByteArrayOutputStream();

        final Document documento =
                new Document(
                        PageSize.A4,
                        42,
                        42,
                        38,
                        42
                );

        try {
            final PdfWriter escritor =
                    PdfWriter.getInstance(
                            documento,
                            salida
                    );

            escritor.setStrictImageSequence(true);

            documento.addTitle(
                    "Receta medica "
                            + formatoNumeroReceta(
                            receta.idRecetas()
                    )
            );

            documento.addSubject(
                    "Prescripcion medica del paciente "
                            + receta.nombrePaciente()
            );

            documento.addAuthor(
                    valorSeguro(
                            receta.nombreProfesional(),
                            receta.nombreConsultorio()
                    )
            );

            documento.addCreator(
                    "Zenticode Medical"
            );

            documento.open();

            agregarEncabezado(
                    documento,
                    receta
            );

            agregarEstadoAnulado(
                    documento,
                    receta
            );

            agregarDatosPaciente(
                    documento,
                    receta
            );

            agregarIndicacionesGenerales(
                    documento,
                    receta
            );

            agregarMedicamentos(
                    documento,
                    receta.medicamentos()
            );

            agregarFirmaProfesional(
                    documento,
                    receta
            );

            agregarPie(
                    documento,
                    receta
            );

            documento.close();

            final byte[] contenido =
                    salida.toByteArray();

            if (contenido.length == 0) {
                throw new IllegalStateException(
                        "El PDF de la receta quedó vacío."
                );
            }

            return contenido;
        } catch (DocumentException excepcion) {
            cerrarDocumento(
                    documento
            );

            throw new IllegalStateException(
                    "No fue posible generar "
                            + "el PDF de la receta.",
                    excepcion
            );
        } catch (RuntimeException excepcion) {
            cerrarDocumento(
                    documento
            );

            throw excepcion;
        }
    }

    // Agrega consultorio, contacto y número de receta.
    private static void agregarEncabezado(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        final PdfPTable encabezado =
                new PdfPTable(
                        new float[]{2.8f, 1.2f}
                );

        encabezado.setWidthPercentage(100);
        encabezado.setSpacingAfter(12);

        final PdfPCell datosConsultorio =
                crearCeldaSinBorde();

        final Paragraph nombreConsultorio =
                new Paragraph(
                        valorSeguro(
                                receta.nombreConsultorio(),
                                "Consultorio médico"
                        ),
                        fuenteTitulo()
                );

        nombreConsultorio.setSpacingAfter(3);

        datosConsultorio.addElement(
                nombreConsultorio
        );

        if (tieneTexto(
                receta.descripcionConsultorio()
        )) {
            final Paragraph descripcion =
                    new Paragraph(
                            receta.descripcionConsultorio(),
                            fuenteSubtitulo()
                    );

            descripcion.setSpacingAfter(3);

            datosConsultorio.addElement(
                    descripcion
            );
        }

        agregarContactoConsultorio(
                datosConsultorio,
                receta
        );

        encabezado.addCell(
                datosConsultorio
        );

        final PdfPCell datosReceta =
                crearCeldaSinBorde();

        final Paragraph titulo =
                new Paragraph(
                        "RECETA MEDICA",
                        fuenteSubtituloPrincipal()
                );

        titulo.setAlignment(
                Element.ALIGN_RIGHT
        );

        datosReceta.addElement(
                titulo
        );

        final Paragraph numero =
                new Paragraph(
                        "Nro. "
                                + formatoNumeroReceta(
                                receta.idRecetas()
                        ),
                        fuenteTextoNegrita()
                );

        numero.setAlignment(
                Element.ALIGN_RIGHT
        );

        datosReceta.addElement(
                numero
        );

        final Paragraph fecha =
                new Paragraph(
                        "Fecha: "
                                + formatearFechaHora(
                                receta.fechaEmision()
                        ),
                        fuenteTextoNormal()
                );

        fecha.setAlignment(
                Element.ALIGN_RIGHT
        );

        datosReceta.addElement(
                fecha
        );

        encabezado.addCell(
                datosReceta
        );

        documento.add(
                encabezado
        );

        agregarSeparador(
                documento
        );
    }

    // Agrega dirección, teléfono y correo disponibles.
    private static void agregarContactoConsultorio(
            final PdfPCell celda,
            final RecetaResponse receta
    ) {
        agregarTextoOpcional(
                celda,
                receta.direccionConsultorio()
        );

        final String telefono =
                tieneTexto(
                        receta.telefonoConsultorio()
                )
                        ? "Teléfono: "
                        + receta.telefonoConsultorio()
                        : null;

        agregarTextoOpcional(
                celda,
                telefono
        );

        final String correo =
                tieneTexto(
                        receta.correoConsultorio()
                )
                        ? "Correo: "
                        + receta.correoConsultorio()
                        : null;

        agregarTextoOpcional(
                celda,
                correo
        );
    }

    // Agrega una línea de contacto opcional.
    private static void agregarTextoOpcional(
            final PdfPCell celda,
            final String texto
    ) {
        if (!tieneTexto(texto)) {
            return;
        }

        final Paragraph parrafo =
                new Paragraph(
                        texto.trim(),
                        fuenteTextoSuave()
                );

        parrafo.setLeading(11);

        celda.addElement(
                parrafo
        );
    }

    // Agrega la línea visual del encabezado.
    private static void agregarSeparador(
            final Document documento
    ) throws DocumentException {
        final PdfPTable separador =
                new PdfPTable(1);

        separador.setWidthPercentage(100);
        separador.setSpacingAfter(14);

        final PdfPCell linea =
                new PdfPCell(
                        new Phrase("")
                );

        linea.setFixedHeight(3);
        linea.setBorder(
                Rectangle.NO_BORDER
        );
        linea.setBackgroundColor(
                COLOR_PRINCIPAL
        );

        separador.addCell(
                linea
        );

        documento.add(
                separador
        );
    }

    // Muestra claramente una receta anulada.
    private static void agregarEstadoAnulado(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        if (receta.estado()
                != EstadoReceta.ANULADA) {
            return;
        }

        final PdfPTable tabla =
                new PdfPTable(1);

        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(12);

        final PdfPCell celda =
                new PdfPCell();

        celda.setPadding(10);
        celda.setBorderColor(
                COLOR_ANULADA
        );
        celda.setBorderWidth(1.5f);
        celda.setBackgroundColor(
                new Color(255, 238, 238)
        );

        final Paragraph estado =
                new Paragraph(
                        "RECETA ANULADA",
                        fuenteAnulada()
                );

        estado.setAlignment(
                Element.ALIGN_CENTER
        );

        celda.addElement(
                estado
        );

        if (tieneTexto(
                receta.motivoAnulacion()
        )) {
            final Paragraph motivo =
                    new Paragraph(
                            "Motivo: "
                                    + receta.motivoAnulacion(),
                            fuenteTextoNormal()
                    );

            motivo.setAlignment(
                    Element.ALIGN_CENTER
            );

            celda.addElement(
                    motivo
            );
        }

        if (receta.fechaAnulacion() != null) {
            final Paragraph fecha =
                    new Paragraph(
                            "Fecha de anulación: "
                                    + formatearFechaHora(
                                    receta.fechaAnulacion()
                            ),
                            fuenteTextoNormal()
                    );

            fecha.setAlignment(
                    Element.ALIGN_CENTER
            );

            celda.addElement(
                    fecha
            );
        }

        tabla.addCell(
                celda
        );

        documento.add(
                tabla
        );
    }

    // Agrega paciente y referencias de la atención.
    private static void agregarDatosPaciente(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        agregarTituloSeccion(
                documento,
                "Datos del paciente"
        );

        final PdfPTable tabla =
                new PdfPTable(
                        new float[]{1.2f, 3.8f}
                );

        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(14);

        agregarFilaDato(
                tabla,
                "Paciente:",
                valorSeguro(
                        receta.nombrePaciente(),
                        "No registrado"
                )
        );

        agregarFilaDato(
                tabla,
                "Consulta:",
                "Nro. "
                        + receta.idConsultas()
        );

        agregarFilaDato(
                tabla,
                "Receta:",
                formatoNumeroReceta(
                        receta.idRecetas()
                )
        );

        agregarFilaDato(
                tabla,
                "Emisión:",
                formatearFechaHora(
                        receta.fechaEmision()
                )
        );

        documento.add(
                tabla
        );
    }

    // Agrega las indicaciones generales.
    private static void agregarIndicacionesGenerales(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        if (!tieneTexto(
                receta.indicacionesGenerales()
        )) {
            return;
        }

        agregarTituloSeccion(
                documento,
                "Indicaciones generales"
        );

        final PdfPTable tabla =
                new PdfPTable(1);

        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(14);

        final PdfPCell celda =
                new PdfPCell(
                        new Phrase(
                                receta.indicacionesGenerales(),
                                fuenteTextoNormal()
                        )
                );

        celda.setPadding(9);
        celda.setBorderColor(
                COLOR_BORDE
        );
        celda.setBackgroundColor(
                new Color(248, 251, 253)
        );

        tabla.addCell(
                celda
        );

        documento.add(
                tabla
        );
    }

    // Agrega los medicamentos prescritos.
    private static void agregarMedicamentos(
            final Document documento,
            final List<MedicamentoResponse> medicamentos
    ) throws DocumentException {
        agregarTituloSeccion(
                documento,
                "Medicamentos prescritos"
        );

        int posicionVisual = 1;

        for (final MedicamentoResponse medicamento
                : medicamentos) {

            final PdfPTable tabla =
                    new PdfPTable(1);

            tabla.setWidthPercentage(100);
            tabla.setKeepTogether(true);
            tabla.setSpacingAfter(10);

            final PdfPCell encabezado =
                    new PdfPCell(
                            new Phrase(
                                    posicionVisual
                                            + ". "
                                            + valorSeguro(
                                            medicamento.medicamento(),
                                            "Medicamento"
                                    ),
                                    fuenteMedicamento()
                            )
                    );

            encabezado.setPadding(8);
            encabezado.setBorderColor(
                    COLOR_PRINCIPAL
            );
            encabezado.setBackgroundColor(
                    COLOR_SECUNDARIO
            );

            tabla.addCell(
                    encabezado
            );

            final PdfPCell contenido =
                    new PdfPCell();

            contenido.setPadding(9);
            contenido.setBorderColor(
                    COLOR_BORDE
            );

            agregarLineaMedicamento(
                    contenido,
                    "Presentación",
                    medicamento.presentacion()
            );

            agregarLineaMedicamento(
                    contenido,
                    "Dosis",
                    medicamento.dosis()
            );

            agregarLineaMedicamento(
                    contenido,
                    "Vía de administración",
                    medicamento.viaAdministracion()
            );

            agregarLineaMedicamento(
                    contenido,
                    "Frecuencia",
                    medicamento.frecuencia()
            );

            agregarLineaMedicamento(
                    contenido,
                    "Duración",
                    medicamento.duracion()
            );

            agregarLineaMedicamento(
                    contenido,
                    "Indicaciones",
                    medicamento.indicaciones()
            );

            tabla.addCell(
                    contenido
            );

            documento.add(
                    tabla
            );

            posicionVisual++;
        }
    }

    // Agrega una propiedad del medicamento.
    private static void agregarLineaMedicamento(
            final PdfPCell celda,
            final String etiqueta,
            final String valor
    ) {
        if (!tieneTexto(valor)) {
            return;
        }

        final Paragraph linea =
                new Paragraph();

        linea.setLeading(15);

        linea.add(
                new Chunk(
                        etiqueta + ": ",
                        fuenteTextoNegrita()
                )
        );

        linea.add(
                new Chunk(
                        valor.trim(),
                        fuenteTextoNormal()
                )
        );

        celda.addElement(
                linea
        );
    }

    // Agrega datos del profesional y espacio de firma.
    private static void agregarFirmaProfesional(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        final Paragraph espacio =
                new Paragraph(" ");

        espacio.setSpacingBefore(20);
        espacio.setSpacingAfter(25);

        documento.add(
                espacio
        );

        final PdfPTable tabla =
                new PdfPTable(
                        new float[]{1f, 1f}
                );

        tabla.setWidthPercentage(88);
        tabla.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        tabla.addCell(
                crearCeldaFirma(
                        "Firma del profesional"
                )
        );

        tabla.addCell(
                crearCeldaFirma(
                        "Sello profesional"
                )
        );

        documento.add(
                tabla
        );

        final Paragraph nombre =
                new Paragraph(
                        valorSeguro(
                                receta.nombreProfesional(),
                                "Profesional responsable"
                        ),
                        fuenteProfesional()
                );

        nombre.setAlignment(
                Element.ALIGN_CENTER
        );
        nombre.setSpacingBefore(10);

        documento.add(
                nombre
        );

        if (tieneTexto(
                receta.especialidadProfesional()
        )) {
            final Paragraph especialidad =
                    new Paragraph(
                            receta.especialidadProfesional(),
                            fuenteTextoSuave()
                    );

            especialidad.setAlignment(
                    Element.ALIGN_CENTER
            );

            documento.add(
                    especialidad
            );
        }

        if (tieneTexto(
                receta.numeroColegiatura()
        )) {
            final Paragraph colegiatura =
                    new Paragraph(
                            "Colegiatura: "
                                    + receta.numeroColegiatura(),
                            fuenteTextoSuave()
                    );

            colegiatura.setAlignment(
                    Element.ALIGN_CENTER
            );

            documento.add(
                    colegiatura
            );
        }
    }

    // Agrega la marca secundaria del sistema.
    private static void agregarPie(
            final Document documento,
            final RecetaResponse receta
    ) throws DocumentException {
        final Paragraph pie =
                new Paragraph(
                        "Documento generado mediante Zenticode Medical"
                                + " | Receta "
                                + formatoNumeroReceta(
                                receta.idRecetas()
                        )
                                + " | "
                                + formatearFecha(
                                receta.fechaEmision()
                        ),
                        fuentePie()
                );

        pie.setAlignment(
                Element.ALIGN_CENTER
        );
        pie.setSpacingBefore(22);

        documento.add(
                pie
        );
    }

    // Agrega el título de una sección.
    private static void agregarTituloSeccion(
            final Document documento,
            final String titulo
    ) throws DocumentException {
        final Paragraph seccion =
                new Paragraph(
                        titulo,
                        fuenteSeccion()
                );

        seccion.setSpacingBefore(2);
        seccion.setSpacingAfter(7);

        documento.add(
                seccion
        );
    }

    // Agrega una fila de datos.
    private static void agregarFilaDato(
            final PdfPTable tabla,
            final String etiqueta,
            final String valor
    ) {
        final PdfPCell celdaEtiqueta =
                new PdfPCell(
                        new Phrase(
                                etiqueta,
                                fuenteTextoNegrita()
                        )
                );

        celdaEtiqueta.setPadding(7);
        celdaEtiqueta.setBorderColor(
                COLOR_BORDE
        );
        celdaEtiqueta.setBackgroundColor(
                new Color(245, 248, 250)
        );

        tabla.addCell(
                celdaEtiqueta
        );

        final PdfPCell celdaValor =
                new PdfPCell(
                        new Phrase(
                                valor,
                                fuenteTextoNormal()
                        )
                );

        celdaValor.setPadding(7);
        celdaValor.setBorderColor(
                COLOR_BORDE
        );

        tabla.addCell(
                celdaValor
        );
    }

    // Crea una celda transparente.
    private static PdfPCell crearCeldaSinBorde() {
        final PdfPCell celda =
                new PdfPCell();

        celda.setBorder(
                Rectangle.NO_BORDER
        );
        celda.setPadding(0);

        return celda;
    }

    // Crea un espacio para firma o sello.
    private static PdfPCell crearCeldaFirma(
            final String etiqueta
    ) {
        final PdfPCell celda =
                new PdfPCell();

        celda.setBorder(
                Rectangle.TOP
        );
        celda.setBorderColor(
                COLOR_TEXTO_SUAVE
        );
        celda.setPaddingTop(8);
        celda.setPaddingLeft(18);
        celda.setPaddingRight(18);
        celda.setMinimumHeight(45);

        final Paragraph texto =
                new Paragraph(
                        etiqueta,
                        fuenteTextoSuave()
                );

        texto.setAlignment(
                Element.ALIGN_CENTER
        );

        celda.addElement(
                texto
        );

        return celda;
    }

    // Verifica los datos mínimos del documento.
    private static void validarRecetaParaPdf(
            final RecetaResponse receta
    ) {
        Objects.requireNonNull(
                receta,
                "La receta es obligatoria."
        );

        if (receta.idRecetas() == null
                || receta.idRecetas() <= 0) {
            throw new IllegalArgumentException(
                    "La receta no tiene "
                            + "un identificador válido."
            );
        }

        if (!tieneTexto(
                receta.nombrePaciente()
        )) {
            throw new IllegalArgumentException(
                    "La receta no tiene "
                            + "un paciente válido."
            );
        }

        if (!tieneTexto(
                receta.nombreConsultorio()
        )) {
            throw new IllegalArgumentException(
                    "La receta no tiene "
                            + "un consultorio válido."
            );
        }

        if (!tieneTexto(
                receta.nombreProfesional()
        )) {
            throw new IllegalArgumentException(
                    "La receta no tiene "
                            + "un profesional emisor válido."
            );
        }

        if (receta.fechaEmision() == null) {
            throw new IllegalArgumentException(
                    "La receta no tiene "
                            + "una fecha de emisión válida."
            );
        }

        if (receta.medicamentos() == null
                || receta.medicamentos().isEmpty()) {
            throw new IllegalStateException(
                    "La receta no contiene medicamentos."
            );
        }
    }

    // Cierra el documento si continúa abierto.
    private static void cerrarDocumento(
            final Document documento
    ) {
        if (documento != null
                && documento.isOpen()) {
            documento.close();
        }
    }

    // Comprueba si existe texto útil.
    private static boolean tieneTexto(
            final String valor
    ) {
        return valor != null
                && !valor.isBlank();
    }

    // Devuelve el texto o un valor alternativo.
    private static String valorSeguro(
            final String valor,
            final String alternativa
    ) {
        return tieneTexto(valor)
                ? valor.trim()
                : alternativa;
    }

    // Formatea la fecha y hora.
    private static String formatearFechaHora(
            final OffsetDateTime fecha
    ) {
        if (fecha == null) {
            return "No registrada";
        }

        return fecha.format(
                FORMATO_FECHA_HORA
        );
    }

    // Formatea únicamente la fecha.
    private static String formatearFecha(
            final OffsetDateTime fecha
    ) {
        if (fecha == null) {
            return "Sin fecha";
        }

        return fecha.format(
                FORMATO_FECHA
        );
    }

    // Construye el número visual de receta.
    private static String formatoNumeroReceta(
            final Long idRecetas
    ) {
        if (idRecetas == null) {
            return "000000";
        }

        return String.format(
                "%06d",
                idRecetas
        );
    }

    private static Font fuenteTitulo() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                16,
                COLOR_PRINCIPAL
        );
    }

    private static Font fuenteSubtitulo() {
        return FontFactory.getFont(
                FontFactory.HELVETICA,
                9,
                COLOR_TEXTO_SUAVE
        );
    }

    private static Font fuenteSubtituloPrincipal() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                13,
                COLOR_PRINCIPAL
        );
    }

    private static Font fuenteSeccion() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                11,
                COLOR_PRINCIPAL
        );
    }

    private static Font fuenteMedicamento() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                10,
                new Color(25, 65, 95)
        );
    }

    private static Font fuenteProfesional() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                9,
                Color.BLACK
        );
    }

    private static Font fuenteTextoNormal() {
        return FontFactory.getFont(
                FontFactory.HELVETICA,
                9,
                Color.BLACK
        );
    }

    private static Font fuenteTextoNegrita() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                9,
                Color.BLACK
        );
    }

    private static Font fuenteTextoSuave() {
        return FontFactory.getFont(
                FontFactory.HELVETICA,
                8,
                COLOR_TEXTO_SUAVE
        );
    }

    private static Font fuentePie() {
        return FontFactory.getFont(
                FontFactory.HELVETICA,
                7,
                COLOR_TEXTO_SUAVE
        );
    }

    private static Font fuenteAnulada() {
        return FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                16,
                COLOR_ANULADA
        );
    }
}