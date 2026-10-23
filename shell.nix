{ pkgs ? import <nixpkgs> {}, ... }:

pkgs.mkShell {
  packages = with pkgs; [jdk25 python314 pipenv nodejs_24];
}

