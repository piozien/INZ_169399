import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import ExcelJS from 'exceljs';
import { CouncilBudgetResponseDto, CouncilTransactionResponseDto } from '@/types/budget.types';

export type DateRange = {
    from: string | null;
    to: string | null;
};

const loadFont = async (url: string): Promise<string> => {
    const response = await fetch(url);
    const blob = await response.blob();
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve((reader.result as string).split(',')[1]);
        reader.readAsDataURL(blob);
    });
};

const formatCurrency = (val: number | undefined) =>
    (val || 0).toLocaleString('pl-PL', { style: 'currency', currency: 'PLN' });

const formatDate = (dateString: string | Date) => new Date(dateString).toLocaleDateString('pl-PL');

export const generateBudgetPdf = async (
    budget: CouncilBudgetResponseDto,
    transactions: CouncilTransactionResponseDto[],
    dateRange?: DateRange
) => {
    const doc = new jsPDF();

    try {
        const fontBase64 = await loadFont('/fonts/Roboto-Regular.ttf');
        doc.addFileToVFS('CustomFont.ttf', fontBase64);
        doc.addFont('CustomFont.ttf', 'CustomFont', 'normal');
        doc.setFont('CustomFont');
    } catch (e) {
        console.warn('Nie udało się załadować polskiej czcionki, używam domyślnej.', e);
    }

    const pageWidth = doc.internal.pageSize.getWidth();
    const margin = 14;
    let yPos = 20;

    doc.setFontSize(18);
    doc.text(`Raport Budżetowy`, pageWidth / 2, yPos, { align: 'center' });
    yPos += 10;

    doc.setFontSize(14);
    doc.text(budget.councilName || 'Samorząd Uczniowski', pageWidth / 2, yPos, { align: 'center' });
    yPos += 15;

    doc.setFontSize(10);
    let periodText = `Rok budżetowy: ${budget.year}`;
    if (dateRange?.from && dateRange?.to) {
        periodText += ` | Okres: ${formatDate(dateRange.from)} - ${formatDate(dateRange.to)}`;
    } else if (dateRange?.from) {
        periodText += ` | Od: ${formatDate(dateRange.from)}`;
    }

    doc.text(periodText, margin, yPos);
    doc.text(`Data wygenerowania: ${new Date().toLocaleDateString('pl-PL')}`, pageWidth - margin, yPos, {
        align: 'right',
    });
    yPos += 10;

    doc.setDrawColor(200);
    doc.line(margin, yPos, pageWidth - margin, yPos);
    yPos += 10;

    const filteredIncome = transactions
        .filter(t => t.type === 'INCOME')
        .reduce((sum, t) => sum + t.amount, 0);
    const filteredExpense = transactions
        .filter(t => t.type === 'EXPENSE')
        .reduce((sum, t) => sum + t.amount, 0);

    doc.text(`Suma przychodów (z raportu): ${formatCurrency(filteredIncome)}`, margin, yPos);
    yPos += 6;
    doc.text(`Suma wydatków (z raportu): ${formatCurrency(filteredExpense)}`, margin, yPos);
    yPos += 15;

    const tableData = transactions.map((t) => [
        formatDate(t.date),
        t.description,
        t.type === 'INCOME' ? 'Wpływ' : 'Wydatek',
        formatCurrency(t.amount),
    ]);

    autoTable(doc, {
        startY: yPos,
        head: [['Data', 'Opis', 'Typ', 'Kwota']],
        body: tableData,
        styles: {
            font: 'CustomFont',
            fontSize: 9,
        },
        headStyles: { fillColor: [255, 157, 0] },
    });

    const fileName = dateRange?.from
        ? `Raport_${budget.year}_${dateRange.from}_${dateRange.to || 'now'}.pdf`
        : `Raport_${budget.year}_pelny.pdf`;

    doc.save(fileName);
};

export const generateBudgetExcel = async (
    budget: CouncilBudgetResponseDto,
    transactions: CouncilTransactionResponseDto[],
    dateRange?: DateRange
) => {
    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Transakcje');

    sheet.addRow([`Raport Budżetowy: ${budget.councilName}`]);
    sheet.addRow([`Rok budżetowy: ${budget.year}`]);
    if (dateRange?.from) {
        sheet.addRow([`Okres: ${formatDate(dateRange.from)} - ${dateRange.to ? formatDate(dateRange.to) : 'teraz'}`]);
    }
    sheet.addRow([]);

    sheet.getRow(1).font = { bold: true, size: 14 };

    sheet.columns = [
        { header: 'Data', key: 'date', width: 15 },
        { header: 'Opis', key: 'description', width: 50 },
        { header: 'Typ', key: 'type', width: 15 },
        { header: 'Kwota', key: 'amount', width: 20, style: { numFmt: '#,##0.00 "PLN"' } },
    ];

    sheet.getRow(5).font = { bold: true };

    transactions.forEach((t) => {
        const row = sheet.addRow({
            date: formatDate(t.date),
            description: t.description,
            type: t.type === 'INCOME' ? 'Wpływ' : 'Wydatek',
            amount: t.amount,
        });

        row.getCell('amount').font = {
            color: { argb: t.type === 'INCOME' ? 'FF008000' : 'FFFF0000' },
        };
    });

    const lastRow = sheet.rowCount + 2;
    const incomeSum = transactions.filter(t => t.type === 'INCOME').reduce((a, b) => a + b.amount, 0);
    const expenseSum = transactions.filter(t => t.type === 'EXPENSE').reduce((a, b) => a + b.amount, 0);

    sheet.getCell(`B${lastRow}`).value = "Suma Przychodów:";
    sheet.getCell(`D${lastRow}`).value = incomeSum;

    sheet.getCell(`B${lastRow + 1}`).value = "Suma Wydatków:";
    sheet.getCell(`D${lastRow + 1}`).value = expenseSum;

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;

    const fileName = dateRange?.from
        ? `Raport_${budget.year}_${dateRange.from}.xlsx`
        : `Raport_${budget.year}_pelny.xlsx`;

    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
};