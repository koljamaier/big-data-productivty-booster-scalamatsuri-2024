{pkgs ? import <nixpkgs> {
  config = {
    packageOverrides = pkgs: {
      sbt = pkgs.sbt.override { jre = pkgs.openjdk17; };
    };
  };
}} :
pkgs.mkShell {
  buildInputs = [
    pkgs.openjdk17
    pkgs.just
    pkgs.direnv
    pkgs.nix-direnv
    pkgs.fzf
    pkgs.docker-compose
    pkgs.nodejs
    pkgs.scala-cli
  ];
}
