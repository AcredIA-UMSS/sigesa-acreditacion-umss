FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY testgen/ ./testgen/
COPY batches.json .

ENV PYTHONPATH=/app
ENV SIGESA_REPO_ROOT=/workspace
ENV SIGESA_RUNTIME=docker
ENV SIGESA_IN_DOCKER=true

ENTRYPOINT ["python", "-m", "testgen"]
CMD ["--help"]
