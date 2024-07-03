console.log('JS file linked');
const source = new EventSource("/biometria");
source.addEventListener('message', e =&gt;  {
  console.log('RECEIVED', e.data);
});