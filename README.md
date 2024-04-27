# Setup

Download and install the package manager [nix](https://nixos.org/download/) to easily install and take care of all the dependencies that you need to run the examples.

If you want to run the bigquery example, you will need to set up a google cloud project and set the `GCS_BUCKET` environment variable (additionally to setting up `gcloud`).

# Running the examples

Use the `just` command to interactively pick on of the examples to run.
Alternitavely, you can run the examples directly by running the following commands:

```sh
just run-spark-example
just run-bigquery-example
just run-kafka-example
```