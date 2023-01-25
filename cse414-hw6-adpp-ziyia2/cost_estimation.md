Since it has clustered index so
I/O cost=1000*1/2=500

Then comes block at a time nested loop join
I/O cost=B(RA)*B(S)=500*2000=1000000

Now comes unclustered nested loop join with S.b=U.b
I/O cost =T(R.S)*T(U)/V(b,U)=16000000000

Total cost=1600100500

