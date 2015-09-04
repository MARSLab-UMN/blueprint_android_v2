//
// Created by Owner on 9/2/2015.
//

#include <fstream>
#include <jni.h>
#include "parameters.h"
#include "libdijkstra.h"
#include <string>

/* to use parameters
parameters = Parameters::instance();
std::string file("./bebop_config.txt");
LoadParameters(file, parameters);

 */

Parameters* parameters;
Dijkstra* dijkstra;



extern "C" {
JNIEXPORT jstring JNICALL
Java_edu_umn_mars_blueprintandroidapp_DijkstraThread_stringFromJNI (JNIEnv *env, jobject object) {

    //create new MowerBluetooth object and call 'go()'
//    jclass jcl = env->FindClass("edu/umn/mars/bluelawn/MowerBluetooth");
//    jobject obj = env->AllocObject(jcl);
//    jmethodID method = env->GetMethodID(jcl, "go", "()Ljava/lang/String;");
//    jobject result = env->CallObjectMethod(obj, method);

    //parse result of 'go()' and save to string
//    const char *str = env->GetStringUTFChars((jstring) result, NULL);
//    char str2[100];
//    str2[0] = '\0';
//    strcat(str2, "Hello from C++ over JNI! Your string is: ");
//    strcat(str2, str);

    const char* str2 = "Hello from C++ over JNI!";

    //pass string back to java
    return env->NewStringUTF(str2);
};

}


void SetupDijkstra() {

    // Read graph files
    char starting_nodes_filename[512], ending_nodes_filename[512], weights_filename[512], rresults_filename[512];
    sprintf ( starting_nodes_filename, "%s/tree/start.txt", parameters->dataset_path.c_str() );
    sprintf ( ending_nodes_filename, "%s/tree/end.txt", parameters->dataset_path.c_str() );
    sprintf ( weights_filename, "%s/tree/inliers.txt", parameters->dataset_path.c_str() );
    sprintf ( rresults_filename, "%s/tree/ransac_results.txt", parameters->dataset_path.c_str() );
    std::ifstream starting_nodes_file ( starting_nodes_filename );
    std::ifstream ending_nodes_file ( ending_nodes_filename );
    std::ifstream weights_file ( weights_filename );
    std::ifstream rresults_file ( rresults_filename );

    int start, end;
    float weight_inv, i2p, i5p;

    // Construct the graph
    dijkstra = new Dijkstra ( parameters->num_of_imgs / parameters->img_subsampling );

    while ( starting_nodes_file >> start && ending_nodes_file >> end ) {

        weights_file >> i5p >> i2p;

        start /= parameters->img_subsampling;
        end /= parameters->img_subsampling;

        double psi, t_x, t_y, t_z;
        rresults_file >> psi >> t_x >> t_y >> t_z;

        if ( ( parameters->img_subsampling * start > parameters->start_of_rotation
               && parameters->img_subsampling * start <= parameters->start_of_stairs )
             || ( parameters->img_subsampling * end > parameters->start_of_rotation
                  && parameters->img_subsampling * end <= parameters->start_of_stairs ) ) {
            weight_inv = 1;
        } else {
            weight_inv = i5p;
        }

        if ( fabs ( psi ) > 15 && fabs ( psi ) < 30 ) {
            weight_inv = 0.1;
        } else if ( fabs ( psi ) > 30 ) {
            weight_inv = 0;
        }

        bool start_r = false, start_t = false;
        bool end_r = false, end_t = false;

        if ( weight_inv > 0 ) {
            ( parameters->img_subsampling * start > parameters->start_of_rotation &&
              parameters->img_subsampling * start < parameters->start_of_stairs )
            ? start_r = true : start_t = true;

            ( parameters->img_subsampling * end > parameters->start_of_rotation &&
              parameters->img_subsampling * end < parameters->start_of_stairs )
            ? end_r = true : end_t = true;

            if ( start_r && end_r ) {
                if ( parameters->img_subsampling * start - parameters->img_subsampling * end < 350 ) {
                    dijkstra->addEdge ( start, end, 1 / weight_inv );
                    dijkstra->addEdge ( end, start, 1 / weight_inv );
                }
            } else if ( start_t && end_t ) {
                // if (t_z > 0)
                // {
                dijkstra->addEdge ( end, start, 1 / weight_inv );
                dijkstra->addEdge ( start, end, 1 );
                // }
            } else if ( start_r != end_r ) {
                if ( i5p > i2p ) { // && t_z > 0)
                    dijkstra->addEdge ( end, start, 1 / weight_inv );
                    dijkstra->addEdge ( start, end, 1 );
                } else if ( i2p > i5p ) {
                    dijkstra->addEdge ( end, start, 1 / weight_inv );
                    dijkstra->addEdge ( start, end, 1 / weight_inv );
                }
            }
        }
    }

}
