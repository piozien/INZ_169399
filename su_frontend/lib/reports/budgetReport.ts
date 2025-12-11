import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import ExcelJS from 'exceljs';
import { CouncilBudgetResponseDto, CouncilTransactionResponseDto } from '@/types/budget.types';

const loadFont = async (url: string): Promise<string> => {
    const response = await fetch(url);
    const blob = await response.blob();
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve((reader.result as string).split(',')[1]);
        reader.readAsDataURL(blob);
    });
};

export const generateBudgetPdf = async (
    budget: CouncilBudgetResponseDto,
    transactions: CouncilTransactionResponseDto[]
) => {
    const doc = new jsPDF();

    try {
        const fontBase64 = await loadFont('/fonts/Roboto-Regular.ttf');
        doc.addFileToVFS('CustomFont.ttf', fontBase64);
        doc.addFont('CustomFont.ttf', 'CustomFont', 'normal');
        doc.setFont('CustomFont');
    } catch (e) {
        console.error('Nie udało się załadować polskiej czcionki, używam domyślnej.', e);
    }

    const formatCurrency = (val: number | undefined) =>
        (val || 0).toLocaleString('pl-PL', { style: 'currency', currency: 'PLN' });

    const pageWidth = doc.internal.pageSize.getWidth();
    const margin = 14;
    let yPos = 20;

    doc.setFontSize(18);
    doc.text(`Raport Budżetowy`, pageWidth / 2, yPos, { align: 'center' });
    yPos += 10;

    doc.setFontSize(14);
    doc.text(budget.councilName, pageWidth / 2, yPos, { align: 'center' });
    yPos += 15;

    doc.setFontSize(10);
    doc.text(`Rok: ${budget.year}`, margin, yPos);
    doc.text(`Data: ${new Date().toLocaleDateString('pl-PL')}`, pageWidth - margin, yPos, {
        align: 'right',
    });
    yPos += 10;

    doc.setDrawColor(200);
    doc.line(margin, yPos, pageWidth - margin, yPos);
    yPos += 10;

    doc.text(`Saldo końcowe: ${formatCurrency(budget.balance)}`, margin, yPos);
    yPos += 6;
    doc.text(`Przychody: ${formatCurrency(budget.totalIncome)}`, margin, yPos);
    yPos += 6;
    doc.text(`Wydatki: ${formatCurrency(budget.totalExpenses)}`, margin, yPos);
    yPos += 15;

    const tableData = transactions.map((t) => [
        new Date(t.date).toLocaleDateString('pl-PL'),
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

    doc.save(`Raport_${budget.year}.pdf`);
};

export const generateBudgetExcel = async (
    budget: CouncilBudgetResponseDto,
    transactions: CouncilTransactionResponseDto[]
) => {
    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Transakcje');

    sheet.columns = [
        { header: 'Data', key: 'date', width: 15 },
        { header: 'Opis', key: 'description', width: 50 },
        { header: 'Typ', key: 'type', width: 15 },
        { header: 'Kwota', key: 'amount', width: 20, style: { numFmt: '#,##0.00 "PLN"' } },
    ];

    transactions.forEach((t) => {
        const row = sheet.addRow({
            date: new Date(t.date).toLocaleDateString('pl-PL'),
            description: t.description,
            type: t.type === 'INCOME' ? 'Wpływ' : 'Wydatek',
            amount: t.amount,
        });
        row.getCell('amount').font = {
            color: { argb: t.type === 'INCOME' ? 'FF008000' : 'FFFF0000' },
        };
    });

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Raport_${budget.year}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
};
