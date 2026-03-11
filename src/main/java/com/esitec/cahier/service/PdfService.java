package com.esitec.cahier.service;

import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.FicheSuivi;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.time.LocalDate;

public class PdfService {

    private static final Font TITRE_FONT  = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    private static final Font SOUS_TITRE  = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(30, 60, 114));
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
    private static final Font BOLD_FONT   = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font SMALL_FONT  = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

    public String genererFiche(FicheSuivi fiche, String cheminSortie) throws Exception {
        Cours cours = fiche.getCours();
        Utilisateur enseignant = fiche.getEnseignant();

        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(cheminSortie));
        doc.open();

        // ── En-tête coloré ──────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell cellHeader = new PdfPCell(new Phrase("FICHE DE SUIVI PÉDAGOGIQUE", TITRE_FONT));
        cellHeader.setBackgroundColor(new BaseColor(30, 60, 114));
        cellHeader.setPadding(15);
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setBorder(Rectangle.NO_BORDER);
        header.addCell(cellHeader);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // ── Infos générales ─────────────────────────────────────────────
        doc.add(new Paragraph("INFORMATIONS GÉNÉRALES", SOUS_TITRE));
        doc.add(new Chunk(new LineSeparator()));
        doc.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 2f});

        ajouterLigne(infoTable, "Cours :", cours.getIntitule());
        ajouterLigne(infoTable, "Classe :", cours.getClasse());
        ajouterLigne(infoTable, "Enseignant :", enseignant.getNomComplet());
        ajouterLigne(infoTable, "Volume horaire prévu :", cours.getVolumeHoraire() + " heures");
        ajouterLigne(infoTable, "Heures effectuées :", fiche.getTotalHeures() + " heures");
        ajouterLigne(infoTable, "Séances validées :", fiche.getNbSeancesValidees() + " / " + fiche.getSeances().size());
        ajouterLigne(infoTable, "Date de génération :", LocalDate.now().toString());

        doc.add(infoTable);
        doc.add(Chunk.NEWLINE);

        // ── Tableau des séances ──────────────────────────────────────────
        doc.add(new Paragraph("DÉTAIL DES SÉANCES", SOUS_TITRE));
        doc.add(new Chunk(new LineSeparator()));
        doc.add(Chunk.NEWLINE);

        PdfPTable seancesTable = new PdfPTable(6);
        seancesTable.setWidthPercentage(100);
        seancesTable.setWidths(new float[]{1.5f, 1f, 1f, 3f, 2f, 1.5f});

        String[] headers = {"Date", "Heure", "Durée (h)", "Contenu", "Observations", "Statut"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
            cell.setBackgroundColor(new BaseColor(52, 73, 94));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            seancesTable.addCell(cell);
        }

        boolean alt = false;
        for (Seance s : fiche.getSeances()) {
            BaseColor bg = alt ? new BaseColor(236, 240, 241) : BaseColor.WHITE;
            ajouterCellule(seancesTable, s.getDate(), bg);
            ajouterCellule(seancesTable, s.getHeure(), bg);
            ajouterCellule(seancesTable, String.valueOf(s.getDuree()), bg);
            ajouterCellule(seancesTable, s.getContenu(), bg);
            ajouterCellule(seancesTable, s.getObservations() != null ? s.getObservations() : "", bg);

            BaseColor statutColor = switch (s.getStatut()) {
                case "VALIDEE" -> new BaseColor(39, 174, 96);
                case "REJETEE" -> new BaseColor(231, 76, 60);
                default        -> new BaseColor(243, 156, 18);
            };
            PdfPCell cellStatut = new PdfPCell(new Phrase(s.getStatut(), new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE)));
            cellStatut.setBackgroundColor(statutColor);
            cellStatut.setPadding(5);
            cellStatut.setHorizontalAlignment(Element.ALIGN_CENTER);
            seancesTable.addCell(cellStatut);
            alt = !alt;
        }

        doc.add(seancesTable);
        doc.add(Chunk.NEWLINE);

        Paragraph footer = new Paragraph("Document généré automatiquement — Cahier de Texte Numérique ESITEC", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return cheminSortie;
    }

    private void ajouterLigne(PdfPTable table, String label, String valeur) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, BOLD_FONT));
        c1.setBorderColor(new BaseColor(189, 195, 199));
        c1.setPadding(6);
        c1.setBackgroundColor(new BaseColor(245, 246, 250));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valeur, NORMAL_FONT));
        c2.setBorderColor(new BaseColor(189, 195, 199));
        c2.setPadding(6);
        table.addCell(c2);
    }

    private void ajouterCellule(PdfPTable table, String texte, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "", SMALL_FONT));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(new BaseColor(189, 195, 199));
        table.addCell(cell);
    }
}
