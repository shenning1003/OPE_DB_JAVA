#include <crypto/ope.hh>
#include <crypto/prng.hh>
#include <crypto/hgd.hh>
#include <crypto/aes.hh>
#include <crypto/sha.hh>
#include <crypto/hmac.hh>
#include <util/zz.hh>
#include <getopt.h>

using namespace std;
using namespace NTL;

/*
 * A gap is represented by the next integer value _above_ the gap.
 */
static ZZ
domain_gap(const ZZ &ndomain, const ZZ &nrange, const ZZ &rgap, PRNG *prng)
{
    return HGD(rgap, ndomain, nrange-ndomain, prng);
}

template<class CB>
ope_domain_range
OPE::lazy_sample(const ZZ &d_lo, const ZZ &d_hi,
                 const ZZ &r_lo, const ZZ &r_hi,
                 CB go_low, blockrng<AES> *prng)  // returns a domain and its ranges in r
{
    ZZ ndomain = d_hi - d_lo + 1;
    ZZ nrange  = r_hi - r_lo + 1;
    throw_c(nrange >= ndomain);

    if (ndomain == 1){ // d_hi == d_lo
        //cout << "r_range: " << r_lo << " -- " << r_hi << endl;
        return ope_domain_range(d_lo, r_lo, r_hi);
	}

    /*
     * Deterministically reset the PRNG counter, regardless of
     * whether we had to use it for HGD or not in previous round.
     */
    auto v = hmac<sha256>::mac(StringFromZZ(d_lo) + "/" +
                               StringFromZZ(d_hi) + "/" +
                               StringFromZZ(r_lo) + "/" +
                               StringFromZZ(r_hi), key);
    v.resize(AES::blocksize);
    prng->set_ctr(v);

    ZZ rgap = nrange/2; // range gap here
    ZZ dgap;

    auto ci = dgap_cache.find(r_lo + rgap); //a (r->d) map. (cipher -> plain)
    if (ci == dgap_cache.end()) { //
        dgap = domain_gap(ndomain, nrange, nrange / 2, prng); // domain gap here
	    /*
        cout <<"-----------------" <<endl;
        cout <<"original dgap"<< dgap <<endl;
        */
        if (dgap > d_hi - d_lo){ // if dgap outside upbound
            dgap = d_hi - d_lo; // fix the dgap generated manually
            if (d_hi - d_lo + 1 == 1){ // if the range is only 1, return
                return ope_domain_range(d_lo, r_lo, r_hi);
            }
            else{ // not needed

            }
        }
        //cout << "out here"<< endl;
        if (dgap < 1){
            dgap = 1;
            if (d_hi - d_lo + 1 == 1){
                return ope_domain_range(d_lo, r_lo, r_hi);
            }
        }
        dgap_cache[r_lo + rgap] = dgap;
    } else {
        dgap = ci->second; 
    }
    /*
    cout <<"-----------------" <<endl;
    cout << "d_lo:"<< d_lo<<endl<<"d_hi:"<<d_hi<<endl<<"dgap"<<dgap<<endl;
    */
    if (go_low(d_lo + dgap, r_lo + rgap)){
        return lazy_sample(d_lo, d_lo + dgap - 1, r_lo, r_lo + rgap - 1, go_low, prng);
	}
    else{
        return lazy_sample(d_lo + dgap, d_hi, r_lo + rgap, r_hi, go_low, prng);
	}
}

template<class CB>
ope_domain_range
OPE::search(CB go_low)
{
    blockrng<AES> r(aesk); // template class blockrng: PRNG, has the function

    return lazy_sample(to_ZZ(0), to_ZZ(1) << pbits,
                       to_ZZ(0), to_ZZ(1) << cbits,
                       go_low, &r);
}

ZZ
OPE::encrypt(const ZZ &ptext)
{
    ope_domain_range dr =
        search([&ptext](const ZZ &d, const ZZ &) { return ptext < d; }); //==go_low()

    auto v = sha256::hash(StringFromZZ(ptext));
    v.resize(16);

    blockrng<AES> aesrand(aesk);
    aesrand.set_ctr(v);

    ZZ nrange = dr.r_hi - dr.r_lo + 1;
    return dr.r_lo + aesrand.rand_zz_mod(nrange);
}

ZZ
OPE::decrypt(const ZZ &ctext)
{
    ope_domain_range dr =
        search([&ctext](const ZZ &, const ZZ &r) { return ctext < r; });
    return dr.d;
}

char* readKey(int n){
  FILE *fp;
  char* out = (char*)malloc(1024);
  char mystring[1024];
  if((fp= fopen("key.txt","r")) == NULL)
    {
      printf("Can not open file! \n");
      exit(0);
    }
  for(int i =1; i <=n; i++)
  {
    fgets(mystring, 1000, fp);
    if(i ==n){
      out = mystring;
      break;
    }
  }
  fclose(fp);
  return out;
}

int main(int argc, char **argv){
  char temp;
  int input;
  string key;
  size_t plainbits, cipherbits;
  ZZ in;
  ZZ output;

  key = readKey(1);
  plainbits = atoi(readKey(2));
  cipherbits = atoi(readKey(3));
  //cout << key <<"\n"<< plainbits << "\n"<<cipherbits<<"\n";
  OPE *ope = new OPE(key, plainbits, cipherbits);

  while((temp = getopt(argc, argv, "e:d:t")))
  {
    switch(temp)
    {
      case 'e':
      input = atoi(argv[2]);
      in = input;
      output = ope->encrypt(in);
      cout<< "The plainText is: " << in << "\n";
      cout<< "The cipher is: "<< output << "\n";
     break;
      case 'd':
     in = to_ZZ(argv[2]);
     cout<< in << "\n";
     output = ope->decrypt(in);
     cout<< "The cipher is: " << in << "\n";
     cout<< "The plainText is: "<< output << "\n";
     break;
      case 't':
      for(ZZ test = to_ZZ("193839877773639"); test < to_ZZ("16897223547984945414");test = test + 10000001123){
          output = ope->decrypt(test);
          cout << "Plaintest is : " << output << endl;
          cout << "CipherTest is : " << test << endl;
      }
    }
    break;
  }
  exit(0);
  return 1;
  /*

  cout<< "abc" <<"\n";
  ZZ input, input2;
  input = 10;
  input2 = 11;
  OPE *ope = new OPE("asdfas", 20, 256);
  //ope->pbits = 1024;
  //ope->cbits = 1024;
  ZZ output, output2;
  output = ope->encrypt(input);
  output2 = ope->encrypt(input2);
  cout<< output << "\n"<< output2 << "\n";
  */
}
