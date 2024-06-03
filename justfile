set shell := ["zsh", "-cu"]
set dotenv-load := true

_default:
  @just --choose

[private]
[no-cd]
setup-scala-cli path:
  @echo 'Setting up Scala CLI'
  cd {{ path }} && scala-cli setup-ide .

run-kafka-example: (setup-scala-cli "kafka") deploy-kafka-infrastructure && open-kafka-ui
  #!/usr/bin/env bash
  set -euxo pipefail
  cd {{ justfile_directory() }}/kafka
  scala-cli run KafkaExample.scala

[private]
deploy-kafka-infrastructure:
  @echo 'Deploying Kafka infrastructure'
  cd {{ justfile_directory() }}/kafka; \
  docker-compose up -d
  @echo "Waiting for Kafka to start..."
  @sleep 5

[private]
[confirm("Open the browser to view the Kafka UI? [y/n]")]
open-kafka-ui: && shut-down-kafka
  open http://localhost:8080

[private]
[confirm("Shut down Kafka stack? [y/n]")]
shut-down-kafka:
  cd {{ justfile_directory() }}/kafka && docker-compose down -v

run-spark-example: download-spark (setup-scala-cli "spark")
  @echo 'Running Spark example'
  cd {{ justfile_directory() }}/spark; \
  scala-cli run --power --spark SparkExample.scala

[private]
download-spark:
  #!/usr/bin/env bash
  set -euxo pipefail
  echo 'Downloading Spark'
  cd {{ justfile_directory() }}/spark
  if [ -d spark-3.5.1-bin-hadoop3-scala2.13 ]; then
    echo 'Spark already downloaded'
    exit 0
  fi
  curl -O https://dlcdn.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3-scala2.13.tgz
  tar -xzf spark-3.5.1-bin-hadoop3-scala2.13.tgz

run-bigquery-example: (setup-scala-cli "bigquery") && gcs-upload bq-create-temp-function
  @echo 'Runnin BigQuery example'
  cd {{ justfile_directory() }}/bigquery; \
  scala-cli --power package BigQueryExample.scala --js-mode dev --force

[private]
gcs-upload:
  @echo 'Uploading compiled JS to GCS bucket'
  gcloud projects list
  @echo $GCS_BUCKET
  gsutil cp {{ justfile_directory() }}/bigquery/main.js gs://$GCS_BUCKET/

[private]
bq-create-temp-function:
  #!/usr/bin/env bash
  set -euxo pipefail
  echo 'Creating a temporary function in BigQuery'
  bq query \
    --use_legacy_sql=false \
    "CREATE TEMP FUNCTION dailyFlavor(f STRING)
    RETURNS STRING
    LANGUAGE js
      OPTIONS (
        library=['gs://$GCS_BUCKET/main.js'])
    AS r\"\"\"
      return dailyFlavor(f)
    \"\"\";
    SELECT dailyFlavor('🥬') AS result;"

