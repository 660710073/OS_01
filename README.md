# OS_01

#คำสั่งในการ run
-cd src
-javac *.java

-java Main jobs_standard.csv fcfs 3 1 2

#เปลี่ยนไปใช้นโยบาย Priority:
-java Main jobs_standard.csv priority 3 1 2

#ทดสอบเปลี่ยนจำนวน Worker:
-java Main jobs_standard.csv priority 1 1 2
-java Main jobs_standard.csv priority 5 1 2

ดสอบจุดคอขวดของทรัพยากร (Printer):
-java Main jobs_printer.csv priority 3 1 2
-java Main jobs_printer.csv priority 3 2 2

