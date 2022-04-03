#For å lage med cmake, må du laste ned CMake of ndk(side by side) ved å gå til
#tools>sdk-manager og deretter sdk tools fanen for så å velge CMake og ndk(side by side) og installere de

sudo apt-get install libudunits2-dev
sudo apt-get install libxml2-dev
sudo apt-get install libudunits2-dev
sudo apt-get install libnetcdf-c++4-dev
sudo apt-get install libproj-dev
sudo apt-get install libeccodes-dev
sudo apt-get install pybind11-dev

#dette skriptet MÅ kjøres i /cpp

git clone https://github.com/HowardHinnant/date.git
git clone https://github.com/metno/fimex.git
cd fimex
mkdir third_party
cd third_party
git clone https://github.com/metno/mi-cpptest
git clone https://github.com/metno/mi-programoptions

#Nå må du checkout branchen hvor fimex bygges, som skriver over CMakeLists.txt filene.

#Herfra skal det være mulig å bruke fimex