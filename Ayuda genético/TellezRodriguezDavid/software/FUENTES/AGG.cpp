#include <iostream>
#include <algorithm>
#include <cstdlib>
#include <vector>
#include "random_ppio.h"
#include <ctime>
#include <limits>
#include <assert.h>
#include <fstream>
#include <cstring>

using namespace std;

int dimensionPatrones, numPatrones, numClusters;

// Suponemos que ambos tienen la misma longitud: dimensionPatrones.
double distanciaEuclidea (vector<double> &A, vector<double> &B) {

	double temp, resultado = 0;

	assert (A.size() == B.size());

	for (int i=0; i<dimensionPatrones; i++) {
		temp = A[i] - B[i];
		resultado += temp*temp;
	}

	return resultado;
}

// Función objetivo: Necesitamos obtener la resta en valor absoluto al cuadrado de cada patrón a su centroide. Si al generar un vecino
// cambiando de clúster un patrón obtenemos que esa distancia ha disminuido es porque es con mayor probabilidad parte de ese clúster.
double funcionObjetivo (vector<vector<int> > &solActual, vector<vector<double> > &patrones, vector<vector<double> > &z) {

	double resultado = 0;

	for (int i = 0; i < solActual.size(); i++) {
		for (vector<int>::iterator it = solActual[i].begin(); it != solActual[i].end(); ++it) 
			resultado += distanciaEuclidea(patrones[*it], z[i]);
	}

	return resultado;
}


char seleccionado (int p, int numCentroides, vector<int> &seleccionados) {

	char seleccionado = 0;
	int k = 0;

	do {

		if (seleccionados[k] == p)
			seleccionado = 1;
		else
			k++;

	} while (!seleccionado && k<numCentroides);

	return seleccionado;
}

int clusterMasCercano (vector<double> &patron, int kInicial, vector<vector<double> > &centroides) {
	
	double distanciaMinima, distanciaActual;
	int cluster;

	distanciaMinima = numeric_limits<double>::max();

	for (int k=0; k<kInicial; k++) {
		distanciaActual = distanciaEuclidea (patron, centroides[k]);

		if (distanciaActual < distanciaMinima) {
			cluster = k;
			distanciaMinima = distanciaActual;
		}
	}
	
	return cluster;
}

bool encontrar (vector<int> &hijo, int base, int tope, int valor) {

	bool res = false;
	while (base < tope && !res) {
		
		if (hijo[base] == valor) {
			res = true;
		}
		base++;
	}
	

	return res;
}

void cruzarPadres (vector<vector<int> > &p, vector<int> *hijo, int padre1,int padre2, int base, int tope) {

	int numAsignaciones = 0, tamano = (*hijo).size();
	
	vector<int>::iterator itPadre1 = p[padre1].begin()+base, itPadre2 = p[padre2].begin()+tope+1;

	while (numAsignaciones < tope-base) {
		(*hijo)[numAsignaciones+base] = *itPadre1;
		itPadre1++;
		numAsignaciones++;
	}

	int contador = tope+1;

	while (numAsignaciones < p[padre1].size()-1) {
		if (!encontrar((*hijo), base, tope, *itPadre2)) {
			if (itPadre2 != p[padre2].end())			
				(*hijo).at(contador) = *itPadre2;

			contador = (contador + 1) % (tamano);
			numAsignaciones++;
		}
		
		if (itPadre2 == p[padre2].end()) {
			itPadre2 = p[padre2].begin();

		}
		else 
			++itPadre2;
	}

}

vector<double> evaluarPoblacion (vector<vector<int> > &p, vector<vector<double> > &z, vector<vector<double> > &patrones, vector<int> *elite) {

	vector<int> m(numClusters);
	double minimo = numeric_limits<double>::max();
	int cluster;
	vector<double> valorJ(p.size());
	vector<vector<vector<int> > > fenotipo(p.size());

	for (int i=0; i<p.size(); i++)
		fenotipo[i].resize(numClusters);

	for (int i=0; i<p.size(); i++) {

		// Agregamos a los clústeres los medoides.
		for (int j=0; j<numClusters; j++) {
			fenotipo[i][j].push_back(p[i][j]);

			m[j] = 1;
			for (int k=0; k<dimensionPatrones; k++)
				z[j][k] = patrones[p[i][j]][k];
		}

		// Se crea la configuración de clústeres siguiendo el esquema del clúster más y recalculando los centroides con cada asignación.
		for (vector<int>::iterator it = p[i].begin()+numClusters; it != p[i].end(); it++) {
			
			cluster = clusterMasCercano(patrones[*it], numClusters, z);
			fenotipo[i][cluster].push_back(*it);

			// Por cada asignación calculamos los centroides.
			for (int k=0; k<z[cluster].size(); k++)
				z[cluster][k] = (m[cluster]*z[cluster][k] + patrones[*it][k])/(m[cluster]+1);

			m[cluster]++;
		}

		valorJ[i] = funcionObjetivo (fenotipo[i], patrones, z);

		if (valorJ[i] < minimo) {
			minimo = valorJ[i];
			(*elite) = p[i];
		}
	}
	
	return valorJ;
}


vector<int> AG (double semilla, vector<vector<double> > &patrones, vector<vector<double> > &z, int tamanoPoblacion) {

	vector<vector<int> > p(tamanoPoblacion),  hijos(tamanoPoblacion);		
	vector<vector<double> > medoides(numClusters);
	vector<double> valorJ(tamanoPoblacion);
	double minimoGlobal = numeric_limits<double>::max();
	vector<int> padres (tamanoPoblacion), elite(numPatrones), mejorSolucion(numPatrones);
	int maxSoluciones = 0, padre1, padre2, parejasCombinar = 0.7*tamanoPoblacion, genesMutar = 0.01*tamanoPoblacion*numPatrones, peorCromosoma;
	
	srand(semilla);
	Set_random(semilla);

	// Inicialización de la población: Aleatoriamente.
	for (int i=0; i<p.size(); i++) {

		for (int j=0; j<patrones.size(); j++)
			p[i].push_back(j);

		random_shuffle(p[i].begin(), p[i].end());
	}

	valorJ = evaluarPoblacion (p, z, patrones, &elite);
	maxSoluciones += 50;

	do {

		// Selección: Torneo binario.
		for (int i=0; i<tamanoPoblacion; i++) {
			padre1 = Randint(0,p.size()-1);
			padre2 = Randint(0,p.size()-1);

			padres[i] = (valorJ[padre1] < valorJ[padre2]) ? padre1 : padre2;
		}

		// Cruce: Operador OX basado en representaciones de orden. Copiamos a la población de padres a los hijos para hacer más sencillo el reemplazo.
		for (int i=0; i<p.size(); i++) {
			hijos[i].resize(numPatrones);
			for (int j=0; j<numPatrones; j++)
				hijos[i][j] = p[i][j];
		}

		for (int i=0; i<parejasCombinar-1; i=i+2) {
			int base = Randint(0,numPatrones-2);
			int tope = Randint(0,numPatrones-2);

			// Ajustamos correctamente el intervalo.
			if (base > tope) {
				int temp = base;
				base = tope;
				tope = temp;
			}
			cruzarPadres (p, &hijos[i], padres[i], padres[i+1], base, tope);
			cruzarPadres (p, &hijos[i+1], padres[i+1], padres[i], base, tope);
		}


		// Mutación: Se elige directamente el número de genes a mutar. Para cada gen a mutar elegimos aleatoriamente un cromosoma y un gen (genMutar) que 
		// intercambiaremos con otro elegido aleatoriamente (patronIntercambio) también.
		for (int i=0; i<genesMutar; i++) {
			int cromosomaMutar = Randint (0,tamanoPoblacion-1), genMutar = Randint (0,numPatrones-1);
			int patronIntercambio = Randint (0,numPatrones-1);

			int temp = p[cromosomaMutar][patronIntercambio];
			p[cromosomaMutar][patronIntercambio] = p[cromosomaMutar][genMutar];
			p[cromosomaMutar][genMutar] = temp;
		}


		// Reemplazamiento de la población actual a partir de la anterior (en hijos se encuentran los hijos calculados y los padres que no se han podido reproducir,
		// es decir, la nueva población completa). 
		for (int i=0; i<p.size(); i++)
			p[i] = hijos[i];


		// Elitismo: Tomamos el mejor cromosoma de nuestra población (elite) y lo reemplazamos por el último cromosoma de la nueva población.
		for (int i=0; i<numPatrones; i++)	
			p[49][i] = elite[i];

		// Evaluación de la nueva población.
		valorJ = evaluarPoblacion (p, z, patrones, &elite);
		maxSoluciones += 50;

		// Calculamos el mínimo para mostrarlo por pantalla y devolver la mejor solución.
		for (int i=0; i<tamanoPoblacion; i++)
			if (valorJ[i] < minimoGlobal) {
				minimoGlobal = valorJ[i];
				mejorSolucion = p[i];
			}

	} while (maxSoluciones < 20000);

	cout << "\n	++ Valor heurístico final: " << minimoGlobal << endl;

	return mejorSolucion;
}

int main (int argc, char* argv[]) {

	// Lectura del nombre de fichero.
	char * fichero = argv[1];
	string leerFichero;
	ifstream archivoDatos (fichero);
	vector<vector<double> > patrones;
	vector<vector<double> > z;
	vector<int> mejorSolucion;

	if (!archivoDatos) {
		cout << "\n(ERROR) El fichero que intenta abrir no existe.\n\n";
		return 1;
	}

	if (strcmp(argv[1],"yeast.txt")==0) {
		numClusters = 10;
		numPatrones = 1484;
		dimensionPatrones = 8;
        	patrones = vector<vector<double> > (1484,vector<double> (8,0));
        	z = vector<vector<double> > (numClusters,vector<double> (8,0));
	}
	else if (strcmp(argv[1],"wdbc.txt")==0) {
		numClusters = 2;
		numPatrones = 569;
		dimensionPatrones = 31;      
        	patrones = vector<vector<double> > (569,vector<double> (31,0));
        	z = vector<vector<double> > (numClusters,vector<double> (31,0)); 
	}
	else if (strcmp(argv[1],"Aggregation.txt")==0) {
		numClusters = 7;
		numPatrones = 788;
		dimensionPatrones = 2;        
        	patrones = vector<vector<double> > (788,vector<double> (2,0));
        	z = vector<vector<double> > (numClusters,vector<double> (2,0));
	}


	for (int i=0; i < patrones.size(); i++)
        	for (int j = 0; j < patrones[i].size(); j++)
          		archivoDatos >> patrones[i][j];

	archivoDatos.close();

	double tiempoInicial = clock();
	mejorSolucion = AG (atof(argv[2]), patrones, z, 50);
	double tiempoFinal = clock();

	cout << "\n	++ Tiempo de ejecución: " << (double)(tiempoFinal-tiempoInicial)/CLOCKS_PER_SEC << endl;

	return 0;
}
