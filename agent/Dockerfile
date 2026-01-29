# Usa uma base Linux com Python 3.10
FROM python:3.10-slim

# Define a pasta de trabalho
WORKDIR /app

# Copia a lista de bibliotecas e instala-as
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia o código do agente (main.py)
COPY . .

# Arranca o Python (o -u serve para veres os prints em tempo real)
CMD ["python", "-u", "main.py"]