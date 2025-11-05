{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [ pkgs.zlib pkgs.graalvmPackages.graalvm-ce pkgs.clang pkgs.python311 ];
}
