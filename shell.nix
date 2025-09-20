{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [ pkgs.zlib pkgs.graalvm-ce pkgs.clang ];
}
