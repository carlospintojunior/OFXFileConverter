package br.com.vulcanum.ofxreader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;

class Transaction {
    String type;
    String date;
    double amount;
    String id;
    String name;

    @Override
    public String toString() {
        return String.format("Type: %s, Date: %s, Amount: %.2f, ID: %s, Name: %s",
                type, date, amount, id, name);
    }

    // Formata os dados para o formato CSV
    public String toCsv() {
        return String.join(",", type, date, String.format("%.2f", amount), id, name);
    }
}

public class OfxReader {

    public static void main(String[] args) {
        // Abrir a interface para selecionar o arquivo
        String filePath = selectFile();
        if (filePath == null) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        // Ler o arquivo OFX e processar as transações
        List<Transaction> transactions = readOfxFile(filePath);

        // Verificar se há transações e salvar em CSV
        if (transactions.isEmpty()) {
            System.out.println("No transactions found in the file.");
        } else {
            System.out.println("Transactions:");
            transactions.forEach(System.out::println);

            // Salvar as transações em um arquivo CSV
            saveToCsv(filePath, transactions);
        }
    }

    private static String selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an OFX File");

        // Adicionar filtro para arquivos OFX
        FileNameExtensionFilter filter = new FileNameExtensionFilter("OFX Files", "ofx");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    private static List<Transaction> readOfxFile(String filePath) {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Transaction currentTransaction = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("<STMTTRN>")) {
                    currentTransaction = new Transaction();
                } else if (line.startsWith("<TRNTYPE>")) {
                    currentTransaction.type = extractValue(line, "TRNTYPE");
                } else if (line.startsWith("<DTPOSTED>")) {
                    currentTransaction.date = extractValue(line, "DTPOSTED");
                } else if (line.startsWith("<TRNAMT>")) {
                    currentTransaction.amount = Double.parseDouble(extractValue(line, "TRNAMT"));
                } else if (line.startsWith("<FITID>")) {
                    currentTransaction.id = extractValue(line, "FITID");
                } else if (line.startsWith("<NAME>")) {
                    currentTransaction.name = extractValue(line, "NAME");
                } else if (line.startsWith("</STMTTRN>")) {
                    transactions.add(currentTransaction);
                    currentTransaction = null;
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading the OFX file: " + e.getMessage());
        }

        return transactions;
    }

    private static String extractValue(String line, String tag) {
        return line.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
    }

    private static void saveToCsv(String ofxFilePath, List<Transaction> transactions) {
        // Criar o nome do arquivo CSV
        String csvFilePath = ofxFilePath.replaceAll("\\.ofx$", ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            // Escrever o cabeçalho
            writer.write("Type,Date,Amount,ID,Name");
            writer.newLine();

            // Escrever as transações
            for (Transaction transaction : transactions) {
                writer.write(transaction.toCsv());
                writer.newLine();
            }

            System.out.println("Transactions saved to CSV: " + csvFilePath);

        } catch (IOException e) {
            System.err.println("Error saving to CSV: " + e.getMessage());
        }
    }
}
